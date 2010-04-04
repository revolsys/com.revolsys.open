package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public final class ShapefileGeometryUtil {
  public static CoordinateSequence[] createCoordinateSequences(
    final int[] partIndex,
    final int dimension) {
    final CoordinateSequence[] parts = new CoordinateSequence[partIndex.length];
    for (int i = 0; i < partIndex.length; i++) {
      final int partNumPoints = partIndex[i];
      parts[i] = new DoubleCoordinatesList(partNumPoints, dimension);
    }
    return parts;
  }

  public static void readCoordinates(
    final EndianInput in,
    final CoordinateSequence coordinates)
    throws IOException {
    for (int j = 0; j < coordinates.size(); j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      coordinates.setOrdinate(j, 0, x);
      coordinates.setOrdinate(j, 1, y);
    }
  }

  public static void readCoordinates(
    final EndianInput in,
    final CoordinateSequence coordinates,
    final int ordinate)
    throws IOException {
    for (int j = 0; j < coordinates.size(); j++) {
      final double d = in.readLEDouble();
      coordinates.setOrdinate(j, ordinate, d);
    }
  }

  public static void readCoordinates(
    final EndianInput in,
    final int[] partIndex,
    final CoordinateSequence[] coordinateSequences,
    final int ordinate)
    throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinateSequence coordinates = coordinateSequences[i];
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

  public static void readPoints(
    final EndianInput in,
    final int[] partIndex,
    final CoordinateSequence[] coordinateSequences)
    throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinateSequence coordinates = coordinateSequences[i];
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
    final CoordinateSequence coordinates)
    throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getOrdinate(i, 0));
      out.writeLEDouble(coordinates.getOrdinate(i, 1));
    }
  }

  public static void write2DCoordinates(
    final EndianOutput out,
    final LineString line)
    throws IOException {
    final CoordinateSequence coordinateSequence = line.getCoordinateSequence();
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
    final LineString line)
    throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = Double.MIN_VALUE;
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    if (coordinates.getDimension() == 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getOrdinate(i, 2);
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
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      if (coordinates.getDimension() == 3) {
        for (int i = 0; i < coordinates.size(); i++) {
          final double z = coordinates.getOrdinate(i, 2);
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
    final CoordinateSequence coordinates)
    throws IOException {
    if (coordinates.getDimension() == 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getOrdinate(i, 2);
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
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    writeCoordinateZValues(out, coordinates);
  }

  public static void writeCoordinateZValues(
    final EndianOutput out,
    final MultiLineString multiLine)
    throws IOException {
    writeCoordinateZRange(out, multiLine);
    for (int n = 0; n < multiLine.getNumGeometries(); n++) {
      final LineString line = (LineString)multiLine.getGeometryN(n);
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      writeCoordinateZValues(out, coordinates);
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
