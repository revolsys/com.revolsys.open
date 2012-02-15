package com.revolsys.io.shp.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class ShapefileGeometryUtil {
  public static List<CoordinatesList> createCoordinatesLists(
    final int[] partIndex,
    final int numAxis) {
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

  public static void readCoordinates(
    final EndianInput in,
    final CoordinatesList points,
    final int ordinate) throws IOException {
    final int size = points.size();
    readCoordinates(in, points, size, ordinate);
  }

  public static void readCoordinates(
    final EndianInput in,
    final CoordinatesList points,
    final int size,
    final int ordinate) throws IOException {
    for (int j = 0; j < size; j++) {
      final double d = in.readLEDouble();
      points.setValue(j, ordinate, d);
    }
  }

  public static void readCoordinates(
    final EndianInput in,
    final int[] partIndex,
    final List<CoordinatesList> coordinateLists,
    final int ordinate) throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readCoordinates(in, coordinates, ordinate);
    }
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

  public static MultiPoint readMultiPatch(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    return null;
  }

  public static MultiPoint readMultiPatchM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    return null;
  }

  public static MultiPoint readMultiPatchZ(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    return null;
  }

  public static MultiPoint readMultiPatchZM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    return null;
  }

  public static MultiPoint readMultipoint(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultipointM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultipointZ(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 3);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public static MultiPoint readMultipointZM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 2);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    ShapefileGeometryUtil.readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public static int[] readPartIndex(
    final EndianInput in,
    final int numParts,
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

  public static Point readPoint(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final CoordinatesList points = readXYCoordinates(in, 1, 2);
    return geometryFactory.createPoint(points);
  }

  public static Point readPointM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = 0;
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z,
      m);
    return geometryFactory.createPoint(points);
  }

  public static void readPoints(
    final EndianInput in,
    final int[] partIndex,
    final List<CoordinatesList> coordinateLists) throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readXYCoordinates(in, coordinates);
    }
  }

  public static Point readPointZ(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z);
    return geometryFactory.createPoint(points);
  }

  public static Point readPointZM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(2, x, y, z,
      m);
    return geometryFactory.createPoint(points);
  }

  public static Polygon readPolygon(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 2);

    readPoints(in, partIndex, parts);
    return geometryFactory.createPolygon(parts);

  }

  public static Polygon readPolygonM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
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

  public static Polygon readPolygonZ(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
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

  public static Polygon readPolygonZM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
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

  public static Geometry readPolyline(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);

      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();

      }
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readXYCoordinates(in, numCoords, numAxis);
        pointsList.add(points);
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public static Geometry readPolylineM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 3);
      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readXYCoordinates(in, numCoords, numAxis);
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

  public static Geometry readPolylineZ(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 2);
      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readXYCoordinates(in, numCoords, numAxis);
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

  public static Geometry readPolylineZM(
    final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
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
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = readXYCoordinates(in, numCoords, numAxis);
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

  public static void readXYCoordinates(
    final EndianInput in,
    final CoordinatesList points) throws IOException {
    for (int j = 0; j < points.size(); j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      points.setX(j, x);
      points.setY(j, y);
    }
  }

  public static CoordinatesList readXYCoordinates(
    final EndianInput in,
    final int numPoints,
    final int numAxis) throws IOException {
    final CoordinatesList points = new DoubleCoordinatesList(numPoints, numAxis);
    readXYCoordinates(in, points);
    return points;
  }

  public static void writeEnvelope(
    final EndianOutput out,
    final Envelope envelope) throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public static void writeMCoordinates(
    final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    if (coordinates.getNumAxis() >= 4) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double m = coordinates.getM(i);
        if (!Double.isNaN(m)) {
          out.writeLEDouble(m);
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

  public static void writeMCoordinates(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeMCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeMCoordinates(out, coordinates);
    }
  }

  public static void writeMCoordinates(
    final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeMCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeMCoordinates(out, points);
    }
  }

  public static void writeMCoordinatesRange(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = Double.MIN_VALUE;
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      if (coordinates.getNumAxis() >= 4) {
        for (int i = 0; i < coordinates.size(); i++) {
          final double m = coordinates.getM(i);
          if (!Double.isNaN(m)) {
            minM = Math.min(minM, m);
            maxM = Math.max(maxM, m);
          }
        }
      }
    }
    if (minM == Double.MAX_VALUE && maxM == Double.MIN_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public static void writeMCoordinatesRange(
    final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = Double.MIN_VALUE;
    for (final CoordinatesList ring : pointsList) {
      for (int i = 0; i < ring.size(); i++) {
        double m = ring.getOrdinate(i, 2);
        if (Double.isNaN(m)) {
          m = 0;
        }
        minM = Math.min(m, minM);
        maxM = Math.max(m, maxM);
      }
    }
    if (minM == Double.MAX_VALUE && maxM == Double.MIN_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public static void writeMultiPatch(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
  }

  public static void writeMultiPatchM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
  }

  public static void writeMultiPatchZ(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
  }

  public static void writeMultiPatchZM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
  }

  public static void writeMultipoint(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.POLYLINE_SHAPE);
  }

  private static void writeMultipoint(
    final EndianOutput out,
    final Geometry geometry,
    final int shapeType) throws IOException {
    if (geometry instanceof MultiPoint) {
      final int numPoints = geometry.getNumPoints();
      final Envelope envelope = geometry.getEnvelopeInternal();
      out.writeLEInt(shapeType);
      ShapefileGeometryUtil.writeEnvelope(out, envelope);
      out.writeLEInt(numPoints);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException("Expecting " + MultiPoint.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writeMultipointM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_M_SHAPE);
    writeMCoordinates(out, geometry);
  }

  public static void writeMultipointZ(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_Z_SHAPE);
    writeZCoordinates(out, geometry);
  }

  public static void writeMultipointZM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_ZM_SHAPE);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public static void writePoint(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      out.writeLEInt(ShapefileConstants.POINT_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writePointM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      out.writeLEInt(ShapefileConstants.POINT_M_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
      out.writeLEDouble(points.getM(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writePointZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      out.writeLEInt(ShapefileConstants.POINT_Z_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
      out.writeLEDouble(points.getZ(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writePointZM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      out.writeLEInt(ShapefileConstants.POINT_ZM_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
      out.writeLEDouble(points.getZ(0));
      out.writeLEDouble(points.getM(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writePolygon(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writePolygon(out, geometry, ShapefileConstants.POLYGON_SHAPE);
  }

  private static List<CoordinatesList> writePolygon(
    final EndianOutput out,
    final Geometry geometry,
    final int shapeType) throws IOException {
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;

      int numPoints = 0;

      final int numHoles = polygon.getNumInteriorRing();

      final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
      final LineString exterior = polygon.getExteriorRing();
      CoordinatesList exteroirPoints = CoordinatesListUtil.get(exterior);
      if (JtsGeometryUtil.isCCW(exteroirPoints)) {
        exteroirPoints = exteroirPoints.reverse();
      }
      rings.add(exteroirPoints);
      numPoints += exteroirPoints.size();
      for (int i = 0; i < numHoles; i++) {
        final LineString interior = polygon.getInteriorRingN(i);
        CoordinatesList interiorCoords = CoordinatesListUtil.get(interior);
        if (!JtsGeometryUtil.isCCW(interiorCoords)) {
          interiorCoords = interiorCoords.reverse();
        }
        rings.add(interiorCoords);
        numPoints += interiorCoords.size();
      }

      final int numParts = 1 + numHoles;

      out.writeLEInt(shapeType);
      final Envelope envelope = polygon.getEnvelopeInternal();
      writeEnvelope(out, envelope);
      out.writeLEInt(numParts);
      out.writeLEInt(numPoints);

      int partIndex = 0;
      for (final CoordinatesList ring : rings) {
        out.writeLEInt(partIndex);
        partIndex += ring.size();
      }

      for (final CoordinatesList ring : rings) {
        writeXYCoordinates(out, ring);
      }
      return rings;
    } else {
      throw new IllegalArgumentException("Expecting " + Polygon.class
        + " geometry got " + geometry.getClass());
    }
  }

  public static void writePolygonM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_M_SHAPE);
    writeMCoordinates(out, rings);
  }

  public static void writePolygonZ(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_Z_SHAPE);
    writeZCoordinates(out, rings);
  }

  public static void writePolygonZM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_ZM_SHAPE);
    writeZCoordinates(out, rings);
    writeMCoordinates(out, rings);
  }

  public static void writePolyline(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_SHAPE);
  }

  private static void writePolyline(
    final EndianOutput out,
    final Geometry geometry,
    final int shapeType) throws IOException {
    if (geometry instanceof LineString || geometry instanceof MultiLineString) {
      final int numCoordinates = geometry.getNumPoints();
      final int numGeometries = geometry.getNumGeometries();
      final Envelope envelope = geometry.getEnvelopeInternal();
      out.writeLEInt(shapeType);
      ShapefileGeometryUtil.writeEnvelope(out, envelope);
      out.writeLEInt(numGeometries);
      out.writeLEInt(numCoordinates);
      writePolylinePartIndexes(out, geometry);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException("Expecting " + LineString.class
        + " or " + MultiLineString.class + " geometry got "
        + geometry.getClass());
    }
  }

  public static void writePolylineM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_M_SHAPE);
    writeMCoordinates(out, geometry);
  }

  public static void writePolylinePartIndexes(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    int partIndex = 0;
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final LineString line = (LineString)geometry.getGeometryN(i);
      out.writeLEInt(partIndex);
      partIndex += line.getNumPoints();
    }
  }

  public static void writePolylineZ(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_Z_SHAPE);
    writeZCoordinates(out, geometry);
  }

  public static void writePolylineZM(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_ZM_SHAPE);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public static void writeXYCoordinates(
    final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getX(i));
      out.writeLEDouble(coordinates.getY(i));
    }
  }

  public static void writeXYCoordinates(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      final CoordinatesList points = CoordinatesListUtil.get(subGeometry);
      writeXYCoordinates(out, points);
    }
  }

  public static void writeZCoordinates(
    final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    if (coordinates.getNumAxis() >= 3) {
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

  public static void writeZCoordinates(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    writeZCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeZCoordinates(out, coordinates);
    }
  }

  public static void writeZCoordinates(
    final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeZCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeZCoordinates(out, points);
    }
  }

  public static void writeZCoordinatesRange(
    final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = Double.MIN_VALUE;
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometryN(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      if (coordinates.getNumAxis() >= 3) {
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

  public static void writeZCoordinatesRange(
    final EndianOutput out,
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
    if (minZ == Double.MAX_VALUE && maxZ == Double.MIN_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

  private ShapefileGeometryUtil() {
  }

}
