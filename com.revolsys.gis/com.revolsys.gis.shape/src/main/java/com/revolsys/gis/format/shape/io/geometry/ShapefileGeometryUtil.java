package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;
import java.util.List;

import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public final class ShapefileGeometryUtil {
  public static CoordinatesList[] createCoordinatesLists(
    final int[] partIndex,
    final int dimension) {
    final CoordinatesList[] parts = new CoordinatesList[partIndex.length];
    for (int i = 0; i < partIndex.length; i++) {
      final int partNumPoints = partIndex[i];
      parts[i] = new DoubleCoordinatesList(partNumPoints, dimension);
    }
    return parts;
  }

  public static void readCoordinates(
    final EndianInput in,
    final CoordinatesList coordinates)
    throws IOException {
    for (int j = 0; j < coordinates.size(); j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      coordinates.setX(j, x);
      coordinates.setY(j, y);
    }
  }

  public static void readCoordinates(
    final EndianInput in,
    final CoordinatesList coordinates,
    final int ordinate)
    throws IOException {
    for (int j = 0; j < coordinates.size(); j++) {
      final double d = in.readLEDouble();
      coordinates.setValue(j, ordinate, d);
    }
  }

  public static void readCoordinates(
    final EndianInput in,
    final int[] partIndex,
    final CoordinatesList[] coordinateSequences,
    final int ordinate)
    throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateSequences[i];
      readCoordinates(in, coordinates, ordinate);
    }
  }

  public static int[] readPartIndex(
    final EndianInput in,
    final int numParts,
    final int numPoints)
    throws IOException {
    final int[] partIndex = new int[numParts];
    int startIndex = in.readLEInt();
    for (int i = 1; i < partIndex.length; i++) {
      final int index = in.readLEInt();
      partIndex[i - 1] = index - startIndex;
      startIndex = index;
    }
    partIndex[partIndex.length - 1] = numPoints - startIndex;
    return partIndex;
  }

  public static int[] readIntArray(
    final EndianInput in,
    final int count)
    throws IOException {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = in.readLEInt();
      values[i] = value;
     }
    return values;
  }

  public static void readPoints(
    final EndianInput in,
    final int[] partIndex,
    final CoordinatesList[] coordinateSequences)
    throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateSequences[i];
      readCoordinates(in, coordinates);
    }
  }

  public static void write2DCoordinates(
    final EndianOutput out,
    final Coordinate[] coordinates)
    throws IOException {
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
    }
  }

  public static void write2DCoordinates(
    final EndianOutput out,
    final CoordinatesList coordinates)
    throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getX(i));
      out.writeLEDouble(coordinates.getY(i));
    }
  }

  public static void write2DCoordinates(
    final EndianOutput out,
    final LineString line)
    throws IOException {
    final CoordinatesList coordinateSequence = CoordinatesListUtil.get(line);
    write2DCoordinates(out, coordinateSequence);
  }

  public static void write2DCoordinates(
    final EndianOutput out,
    final MultiLineString multiLine)
    throws IOException {
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multiLine.getGeometryN(i);
      write2DCoordinates(out, line);
    }
  }

  public static void writeCoordinateZRange(
    final EndianOutput out,
    final List<CoordinatesList> pointsList)
    throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = Double.MIN_VALUE;
    for (final CoordinatesList ring : pointsList) {
      for (int i = 0; i < ring.size(); i++) {
        double z = ring.getOrdinate(i, 2);
        if (Double.isNaN(z)) {
          z = 0;
        }
        minZ = Math.min(z, minZ);
        maxZ = Math.max(z, maxZ);
      }
    }
  }

  public static void writeCoordinateZRange(
    final EndianOutput out,
    final LineString line)
    throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = Double.MIN_VALUE;
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    if (coordinates.getNumAxis() == 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getZ(i);
        if (!Double.isNaN(z)) {
          minZ = Math.min(minZ, z);
          maxZ = Math.max(maxZ, z);
        }
      }
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    } else {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    }
  }

  public static void writeCoordinateZRange(
    final EndianOutput out,
    final MultiLineString multiLine)
    throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = Double.MIN_VALUE;
    for (int n = 0; n < multiLine.getNumGeometries(); n++) {
      final LineString line = (LineString)multiLine.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(line);
      if (coordinates.getNumAxis() == 3) {
        for (int i = 0; i < coordinates.size(); i++) {
          final double z = coordinates.getZ(i);
          if (!Double.isNaN(z)) {
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
          }
        }
      }
    }
    if (minZ == Double.MAX_VALUE && maxZ == Double.MIN_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

  public static void writeCoordinateZValues(
    final EndianOutput out,
    final CoordinatesList coordinates)
    throws IOException {
    if (coordinates.getNumAxis() == 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getZ(i);
        if (!Double.isNaN(z)) {
          out.writeLEDouble(z);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < coordinates.size(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public static void writeCoordinateZValues(
    final EndianOutput out,
    final LineString line)
    throws IOException {
    writeCoordinateZRange(out, line);
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    writeCoordinateZValues(out, coordinates);
  }

  public static void writeCoordinateZValues(
    final EndianOutput out,
    final MultiLineString multiLine)
    throws IOException {
    writeCoordinateZRange(out, multiLine);
    for (int n = 0; n < multiLine.getNumGeometries(); n++) {
      final LineString line = (LineString)multiLine.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(line);
      writeCoordinateZValues(out, coordinates);
    }
  }

  public static void writeCoordinateZValues(
    final EndianOutput out,
    final List<CoordinatesList> pointsList)
    throws IOException {
    writeCoordinateZRange(out, pointsList);
    for (CoordinatesList points : pointsList) {
      writeCoordinateZValues(out, points);
    }
  }

  public static void writeEnvelope(
    final EndianOutput out,
    final Envelope envelope)
    throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public static void writePartIndexes(
    final EndianOutput out,
    final MultiLineString multiLine)
    throws IOException {
    int partIndex = 0;
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multiLine.getGeometryN(i);
      out.writeLEInt(partIndex);
      partIndex += line.getNumPoints();
    }
  }

  private ShapefileGeometryUtil() {
  }

}
