package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class ShapefileGeometryUtil {
  public static List<CoordinatesList> createCoordinatesLists(
    final int[] partIndex, final int numAxis) {
    final List<CoordinatesList> parts = new ArrayList<CoordinatesList>(
      partIndex.length);
    for (int i = 0; i < partIndex.length; i++) {
      final int partNumPoints = partIndex[i];
      final DoubleCoordinatesList points = new DoubleCoordinatesList(
        partNumPoints, numAxis);
      parts.add(points);
    }
    return parts;
  }

  public static Point readPoint(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final CoordinatesList points = readCoordinates(in, 1, 2);
    return geometryFactory.createPoint(points);
  }

  public static Point readPointZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z);
    return geometryFactory.createPoint(points);
  }

  public static Point readPointM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = 0;
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z,
      m);
    return geometryFactory.createPoint(points);
  }

  public static Point readPointZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z,
      m);
    return geometryFactory.createPoint(points);
  }

  public static MultiPoint readMultiPoint(
    final GeometryFactory geometryFactory, final EndianInput in)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readCoordinates(in, numPoints, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultiPointZ(
    final GeometryFactory geometryFactory, final EndianInput in)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readCoordinates(in, numPoints, 3);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultiPointZM(
    final GeometryFactory geometryFactory, final EndianInput in)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 2);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultiPointM(
    final GeometryFactory geometryFactory, final EndianInput in)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public static Geometry readPolyline(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readCoordinates(in, numPoints, numAxis);

      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();

      }
      List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readCoordinates(in, numCoords, numAxis);
        pointsList.add(points);
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public static CoordinatesList readCoordinates(final EndianInput in,
    final int numPoints, final int numAxis) throws IOException {
    final CoordinatesList points = new DoubleCoordinatesList(numPoints, numAxis);
    readCoordinates(in, points);
    return points;
  }

  public static Geometry readPolylineZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 2);
      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readCoordinates(in, numCoords, numAxis);
        pointsList.add(points);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final CoordinatesList points = pointsList.get(i);
        readCoordinates(in, points, points.size(), 2);
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public static Geometry readPolylineZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 2);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 3);
      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readCoordinates(in, numCoords, numAxis);
        pointsList.add(points);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final CoordinatesList points = pointsList.get(i);
        readCoordinates(in, points, points.size(), 2);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final CoordinatesList points = pointsList.get(i);
        readCoordinates(in, points, points.size(), 3);
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public static Geometry readPolylineM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 3);
      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readCoordinates(in, numCoords, numAxis);
        pointsList.add(points);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final CoordinatesList points = pointsList.get(i);
        readCoordinates(in, points, points.size(), 3);
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public static void readCoordinates(final EndianInput in,
    final CoordinatesList points) throws IOException {
    for (int j = 0; j < points.size(); j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      points.setX(j, x);
      points.setY(j, y);
    }
  }

  public static void readCoordinates(final EndianInput in,
    final CoordinatesList points, final int ordinate) throws IOException {
    final int size = points.size();
    readCoordinates(in, points, size, ordinate);
  }

  public static void readCoordinates(final EndianInput in,
    final CoordinatesList points, final int size, final int ordinate)
    throws IOException {
    for (int j = 0; j < size; j++) {
      final double d = in.readLEDouble();
      points.setValue(j, ordinate, d);
    }
  }

  public static void readCoordinates(final EndianInput in,
    final int[] partIndex, final List<CoordinatesList> coordinateLists,
    final int ordinate) throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readCoordinates(in, coordinates, ordinate);
    }
  }

  public static int[] readPartIndex(final EndianInput in, final int numParts,
    final int numPoints) throws IOException {
    final int[] partIndex = new int[numParts];
    if (numParts > 0) {
      int startIndex = in.readLEInt();
      for (int i = 1; i < partIndex.length; i++) {
        final int index = in.readLEInt();
        partIndex[i - 1] = index - startIndex;
        startIndex = index;
      }
      partIndex[partIndex.length - 1] = numPoints - startIndex;
    }
    return partIndex;
  }

  public static int[] readIntArray(final EndianInput in, final int count)
    throws IOException {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = in.readLEInt();
      values[i] = value;
    }
    return values;
  }

  public static void readPoints(final EndianInput in, final int[] partIndex,
    final List<CoordinatesList> coordinateLists) throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readCoordinates(in, coordinates);
    }
  }

  public static void write2DCoordinates(final EndianOutput out,
    final Coordinate[] coordinates) throws IOException {
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
    }
  }

  public static void write2DCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getX(i));
      out.writeLEDouble(coordinates.getY(i));
    }
  }

  public static void write2DCoordinates(final EndianOutput out,
    final LineString line) throws IOException {
    final CoordinatesList coordinateSequence = CoordinatesListUtil.get(line);
    write2DCoordinates(out, coordinateSequence);
  }

  public static void write2DCoordinates(final EndianOutput out,
    final MultiLineString multiLine) throws IOException {
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multiLine.getGeometryN(i);
      write2DCoordinates(out, line);
    }
  }

  public static void writeCoordinateZRange(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
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

  public static void writeCoordinateZRange(final EndianOutput out,
    final LineString line) throws IOException {
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

  public static void writeCoordinateZRange(final EndianOutput out,
    final MultiLineString multiLine) throws IOException {
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

  public static void writeCoordinateZValues(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
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

  public static void writeCoordinateZValues(final EndianOutput out,
    final LineString line) throws IOException {
    writeCoordinateZRange(out, line);
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    writeCoordinateZValues(out, coordinates);
  }

  public static void writeCoordinateZValues(final EndianOutput out,
    final MultiLineString multiLine) throws IOException {
    writeCoordinateZRange(out, multiLine);
    for (int n = 0; n < multiLine.getNumGeometries(); n++) {
      final LineString line = (LineString)multiLine.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(line);
      writeCoordinateZValues(out, coordinates);
    }
  }

  public static void writeCoordinateZValues(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeCoordinateZRange(out, pointsList);
    for (CoordinatesList points : pointsList) {
      writeCoordinateZValues(out, points);
    }
  }

  public static void writeEnvelope(final EndianOutput out,
    final Envelope envelope) throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public static void writePartIndexes(final EndianOutput out,
    final MultiLineString multiLine) throws IOException {
    int partIndex = 0;
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multiLine.getGeometryN(i);
      out.writeLEInt(partIndex);
      partIndex += line.getNumPoints();
    }
  }

  private ShapefileGeometryUtil() {
  }

  public static Polygon readPolygon(GeometryFactory geometryFactory,
    EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 2);

    readPoints(in, partIndex, parts);
    return geometryFactory.createPolygon(parts);

  }

  public static Polygon readPolygonZ(GeometryFactory geometryFactory,
    EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = ShapefileGeometryUtil.readPartIndex(in, numParts,
      numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 3);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 2);
    return geometryFactory.createPolygon(parts);
  }

  public static Polygon readPolygonZM(GeometryFactory geometryFactory,
    EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = ShapefileGeometryUtil.readPartIndex(in, numParts,
      numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 2);
    readCoordinates(in, partIndex, parts, 3);
    return geometryFactory.createPolygon(parts);
  }

  public static Polygon readPolygonM(GeometryFactory geometryFactory,
    EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = ShapefileGeometryUtil.readPartIndex(in, numParts,
      numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 3);
    return geometryFactory.createPolygon(parts);
  }

}
