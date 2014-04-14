package com.revolsys.io.shp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;

public final class ShapefileGeometryUtil {
  public static final Map<String, Method> GEOMETRY_TYPE_READ_METHOD_MAP = new LinkedHashMap<String, Method>();

  public static final Map<String, Method> GEOMETRY_TYPE_WRITE_METHOD_MAP = new LinkedHashMap<String, Method>();
  static {
    addReadWriteMethods("Point");
    addReadWriteMethods("Polygon");
    addReadWriteMethods("Polyline");
    addReadWriteMethods("Multipoint");

    for (final boolean z : Arrays.asList(false, true)) {
      for (final boolean m : Arrays.asList(false, true)) {
        final String hasZ = String.valueOf(z).toUpperCase();
        final String hasM = String.valueOf(m).toUpperCase();
        GEOMETRY_TYPE_READ_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYGON" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYGON" + hasZ + hasM));
      }
    }

  }

  public static final ShapefileGeometryUtil INSTANCE = new ShapefileGeometryUtil();

  public static final ShapefileGeometryUtil SHP_INSTANCE = new ShapefileGeometryUtil(
    true, true);

  private static void addMethod(final String action,
    final Map<String, Method> methodMap, final String geometryType,
    final boolean hasZ, final boolean hasM, final Class<?>... parameterTypes) {
    final String geometryTypeKey = (geometryType + hasZ + hasM).toUpperCase();
    String methodName = action + geometryType;
    if (hasZ) {
      methodName += "Z";
    }
    if (hasM) {
      methodName += "M";
    }
    final Method method = JavaBeanUtil.getMethod(ShapefileGeometryUtil.class,
      methodName, parameterTypes);
    methodMap.put(geometryTypeKey, method);
  }

  private static void addReadWriteMethods(final String geometryType) {
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false,
      false, GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, false,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false, true,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, true,
      GeometryFactory.class, EndianInput.class);

    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      true, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      true, EndianOutput.class, Geometry.class);
  }

  public static Method getReadMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_READ_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Reader for: "
        + geometryTypeKey);
    }
    return method;
  }

  public static int getShapeType(final Geometry geometry) {
    if (geometry != null) {
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      final int numAxis = geometryFactory.getNumAxis();
      final boolean hasZ = numAxis > 2;
      final boolean hasM = numAxis > 3;

      if (geometry instanceof Point) {
        if (hasM) {
          return ShapefileConstants.POINT_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POINT_Z_SHAPE;
        } else {
          return ShapefileConstants.POINT_SHAPE;
        }
      } else if (geometry instanceof MultiPoint) {
        if (hasM) {
          return ShapefileConstants.MULTI_POINT_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.MULTI_POINT_Z_SHAPE;
        } else {
          return ShapefileConstants.MULTI_POINT_SHAPE;
        }
      } else if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        if (hasM) {
          return ShapefileConstants.POLYLINE_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POLYLINE_Z_SHAPE;
        } else {
          return ShapefileConstants.POLYLINE_SHAPE;
        }
      } else if ((geometry instanceof Polygon)
        || (geometry instanceof MultiPolygon)) {
        if (hasM) {
          return ShapefileConstants.POLYGON_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POLYGON_Z_SHAPE;
        } else {
          return ShapefileConstants.POLYGON_SHAPE;
        }
      } else {
        throw new IllegalArgumentException("Unsupported geometry type: "
          + geometry.getGeometryType());
      }
    }
    return ShapefileConstants.NULL_SHAPE;
  }

  public static Method getWriteMethod(final Geometry geometry) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    final int numAxis = geometryFactory.getNumAxis();
    final boolean hasZ = numAxis > 2;
    final boolean hasM = numAxis > 3;
    final String geometryType = geometry.getGeometryType();
    final String geometryTypeKey = geometryType.toUpperCase() + hasZ + hasM;
    return getWriteMethod(geometryTypeKey);
  }

  public static Method getWriteMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_WRITE_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Writer for: "
        + geometryTypeKey);
    }
    return method;
  }

  private boolean clockwise = true;

  private boolean writeLength = false;

  public ShapefileGeometryUtil() {

  }

  public ShapefileGeometryUtil(final boolean clockwise) {
    this.clockwise = clockwise;
  }

  public ShapefileGeometryUtil(final boolean clockwise,
    final boolean writeLength) {
    this.clockwise = clockwise;
    this.writeLength = writeLength;
  }

  public List<CoordinatesList> createCoordinatesLists(final int[] partIndex,
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

  public Geometry createPolygonGeometryFromParts(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final List<CoordinatesList> parts) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    final List<CoordinatesList> currentParts = new ArrayList<CoordinatesList>();
    for (final CoordinatesList ring : parts) {
      final boolean ringClockwise = !CoordinatesListUtil.isCCW(ring);
      if (ringClockwise == clockwise) {
        if (!currentParts.isEmpty()) {
          final Polygon polygon = geometryFactory.createPolygon(currentParts);
          polygons.add(polygon);
          currentParts.clear();
        }
      }
      currentParts.add(ring);
    }
    if (!currentParts.isEmpty()) {
      final Polygon polygon = geometryFactory.createPolygon(currentParts);
      polygons.add(polygon);
    }
    if (polygons.size() == 1) {
      return polygons.get(0);
    } else {
      return geometryFactory.createMultiPolygon(polygons);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V read(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in, final int shapeType) throws IOException {
    switch (shapeType) {
      case ShapefileConstants.NULL_SHAPE:
        return null;
      case ShapefileConstants.POINT_SHAPE:
        return (V)readPoint(geometryFactory, in);
      case ShapefileConstants.POINT_M_SHAPE:
        return (V)readPointM(geometryFactory, in);
      case ShapefileConstants.POINT_Z_SHAPE:
        return (V)readPointZ(geometryFactory, in);
      case ShapefileConstants.POINT_ZM_SHAPE:
        return (V)readPointZM(geometryFactory, in);

      case ShapefileConstants.MULTI_POINT_SHAPE:
        return (V)readMultipoint(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_M_SHAPE:
        return (V)readMultipointM(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_Z_SHAPE:
        return (V)readMultipointZ(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
        return (V)readMultipointZM(geometryFactory, in);

      case ShapefileConstants.POLYLINE_SHAPE:
        return (V)readPolyline(geometryFactory, in);
      case ShapefileConstants.POLYLINE_M_SHAPE:
        return (V)readPolylineM(geometryFactory, in);
      case ShapefileConstants.POLYLINE_Z_SHAPE:
        return (V)readPolylineZ(geometryFactory, in);
      case ShapefileConstants.POLYLINE_ZM_SHAPE:
        return (V)readPolylineZM(geometryFactory, in);

      case ShapefileConstants.POLYGON_SHAPE:
        return (V)readPolygon(geometryFactory, in);
      case ShapefileConstants.POLYGON_M_SHAPE:
        return (V)readPolygonM(geometryFactory, in);
      case ShapefileConstants.POLYGON_Z_SHAPE:
        return (V)readPolygonZ(geometryFactory, in);
      case ShapefileConstants.POLYGON_ZM_SHAPE:
        return (V)readPolygonZM(geometryFactory, in);
      default:
        throw new IllegalArgumentException(
          "Shapefile shape type not supported: " + shapeType);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V read(final Method method,
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) {
    return (V)JavaBeanUtil.method(method, this, geometryFactory, in);
  }

  public void readCoordinates(final EndianInput in,
    final CoordinatesList points, final int ordinate) throws IOException {
    final int size = points.size();
    readCoordinates(in, points, size, ordinate);
  }

  public void readCoordinates(final EndianInput in,
    final CoordinatesList points, final int size, final int ordinate)
    throws IOException {
    for (int j = 0; j < size; j++) {
      final double d = in.readLEDouble();
      points.setValue(j, ordinate, d);
    }
  }

  public void readCoordinates(final EndianInput in, final int[] partIndex,
    final List<CoordinatesList> coordinateLists, final int ordinate)
    throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readCoordinates(in, coordinates, ordinate);
    }
  }

  public int[] readIntArray(final EndianInput in, final int count)
    throws IOException {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = in.readLEInt();
      values[i] = value;
    }
    return values;
  }

  public MultiPoint readMultipoint(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public MultiPoint readMultipointM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public MultiPoint readMultipointZ(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 3);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, points, 2);
    return geometryFactory.createMultiPoint(points);
  }

  public MultiPoint readMultipointZM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList points = readXYCoordinates(in, numPoints, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, points, 2);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, points, 3);
    return geometryFactory.createMultiPoint(points);
  }

  public int[] readPartIndex(final EndianInput in, final int numParts,
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

  public Point readPoint(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final CoordinatesList points = readXYCoordinates(in, 1, 2);
    return geometryFactory.point(points);
  }

  public Point readPointM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = 0;
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(4, x, y, z,
      m);
    return geometryFactory.point(points);
  }

  public void readPoints(final EndianInput in, final int[] partIndex,
    final List<CoordinatesList> coordinateLists) throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final CoordinatesList coordinates = coordinateLists.get(i);
      readXYCoordinates(in, coordinates);
    }
  }

  public Point readPointZ(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(3, x, y, z);
    return geometryFactory.point(points);
  }

  public Point readPointZM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(4, x, y, z,
      m);
    return geometryFactory.point(points);
  }

  public Geometry readPolygon(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 2);

    readPoints(in, partIndex, parts);

    return createPolygonGeometryFromParts(geometryFactory, parts);

  }

  public Geometry readPolygonM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 3);
    return createPolygonGeometryFromParts(geometryFactory, parts);

  }

  public Geometry readPolygonZ(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 3);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 2);
    return createPolygonGeometryFromParts(geometryFactory, parts);
  }

  public Geometry readPolygonZM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, numPoints);

    final List<CoordinatesList> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts);
    readCoordinates(in, partIndex, parts, 2);
    readCoordinates(in, partIndex, parts, 3);
    return createPolygonGeometryFromParts(geometryFactory, parts);
  }

  public Geometry readPolyline(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 2;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);

      return geometryFactory.lineString(points);
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

  public Geometry readPolylineM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 4;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 3);
      return geometryFactory.lineString(points);
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

  public Geometry readPolylineZ(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 3;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 2);
      return geometryFactory.lineString(points);
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

  public Geometry readPolylineZM(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int numAxis = 4;
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = readXYCoordinates(in, numPoints, numAxis);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 2);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, points, 3);
      return geometryFactory.lineString(points);
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

  public void readXYCoordinates(final EndianInput in,
    final CoordinatesList points) throws IOException {
    for (int j = 0; j < points.size(); j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      points.setX(j, x);
      points.setY(j, y);
    }
  }

  public CoordinatesList readXYCoordinates(final EndianInput in,
    final int numPoints, final int numAxis) throws IOException {
    final CoordinatesList points = new DoubleCoordinatesList(numPoints, numAxis);
    readXYCoordinates(in, points);
    return points;
  }

  public void write(final Method method, final EndianOutput out,
    final Geometry geometry) {
    JavaBeanUtil.method(method, this, out, geometry);
  }

  public void writeEnvelope(final EndianOutput out, final Envelope envelope)
    throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public void writeMCoordinates(final EndianOutput out,
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

  public void writeMCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeMCoordinates(out, coordinates);
    }
  }

  public void writeMCoordinates(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeMCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeMCoordinates(out, points);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
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
    if (minM == Double.MAX_VALUE && maxM == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
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
    if (minM == Double.MAX_VALUE && maxM == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMultipoint(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writeMultipoint(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int wordsPerPoint) throws IOException {
    if (geometry instanceof MultiPoint || geometry instanceof Point) {
      final int numPoints = geometry.getNumPoints();
      if (writeLength) {
        final int recordLength = 20 + wordsPerPoint * numPoints;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE + BYTES_IN_INT +
        // (numPoints * 2 * BYTES_IN_DOUBLE)) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      final Envelope envelope = geometry.getEnvelopeInternal();
      out.writeLEInt(shapeType);
      writeEnvelope(out, envelope);
      out.writeLEInt(numPoints);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException("Expecting " + MultiPoint.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writeMultipointM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writeMultipointZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_Z_SHAPE, 12);
    writeZCoordinates(out, geometry);
  }

  public void writeMultipointZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writePoint(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      if (writeLength) {
        final int recordLength = 10;
        // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePointM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      if (writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_M_SHAPE);
      out.writeLEDouble(points.getX(0));
      out.writeLEDouble(points.getY(0));
      out.writeLEDouble(points.getM(0));
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
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

  public void writePointZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinatesList points = CoordinatesListUtil.get(point);
      if (writeLength) {
        final int recordLength = 18;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
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

  public void writePolygon(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolygon(out, geometry, ShapefileConstants.POLYGON_SHAPE, 0, 8);
  }

  private List<CoordinatesList> writePolygon(final EndianOutput out,
    final Geometry geometry, final int shapeType, final int headerOverhead,
    final int wordsPerPoint) throws IOException {

    int numPoints = 0;

    final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometry(i);
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        final LineString exterior = polygon.getExteriorRing();
        CoordinatesList exteroirPoints = CoordinatesListUtil.get(exterior);
        final boolean exteriorClockwise = !JtsGeometryUtil.isCCW(exteroirPoints);
        if (exteriorClockwise != clockwise) {
          exteroirPoints = exteroirPoints.reverse();
        }
        rings.add(exteroirPoints);
        numPoints += exteroirPoints.size();
        final int numHoles = polygon.getNumInteriorRing();
        for (int j = 0; j < numHoles; j++) {
          final LineString interior = polygon.getInteriorRingN(j);
          CoordinatesList interiorCoords = CoordinatesListUtil.get(interior);
          final boolean interiorClockwise = !JtsGeometryUtil.isCCW(interiorCoords);
          if (interiorClockwise == clockwise) {
            interiorCoords = interiorCoords.reverse();
          }
          rings.add(interiorCoords);
          numPoints += interiorCoords.size();
        }
      } else {
        throw new IllegalArgumentException("Expecting " + Polygon.class
          + " geometry got " + part.getClass());
      }
    }
    final int numParts = rings.size();

    if (writeLength) {
      final int recordLength = 22 + headerOverhead + 2 * numParts
        + wordsPerPoint * numPoints;

      out.writeInt(recordLength);
    }
    out.writeLEInt(shapeType);
    final Envelope envelope = geometry.getEnvelopeInternal();
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
  }

  public void writePolygonM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_M_SHAPE, 8, 12);
    writeMCoordinates(out, rings);
  }

  public void writePolygonZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_Z_SHAPE, 8, 12);
    writeZCoordinates(out, rings);
  }

  public void writePolygonZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_ZM_SHAPE, 16, 16);
    writeZCoordinates(out, rings);
    writeMCoordinates(out, rings);
  }

  public void writePolyline(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writePolyline(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int wordsPerPoint) throws IOException {
    if (geometry instanceof LineString || geometry instanceof MultiLineString) {
      final int numCoordinates = geometry.getNumPoints();
      final int numGeometries = geometry.getNumGeometries();
      final Envelope envelope = geometry.getEnvelopeInternal();

      if (writeLength) {
        // final int recordLength = ((3 + numGeometries) * BYTES_IN_INT + (4 + 2
        // * numCoordinates)
        // * BYTES_IN_DOUBLE) / 2;
        final int recordLength = 22 + numGeometries * 2 + numCoordinates
          * wordsPerPoint;
        out.writeInt(recordLength);
      }
      out.writeLEInt(shapeType);
      writeEnvelope(out, envelope);
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

  public void writePolylineM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writePolylinePartIndexes(final EndianOutput out,
    final Geometry geometry) throws IOException {
    int partIndex = 0;
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final LineString line = (LineString)geometry.getGeometry(i);
      out.writeLEInt(partIndex);
      partIndex += line.getNumPoints();
    }
  }

  public void writePolylineZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_Z_SHAPE, 12);
    writeZCoordinates(out, geometry);
  }

  public void writePolylineZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writeXYCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getX(i));
      out.writeLEDouble(coordinates.getY(i));
    }
  }

  public void writeXYCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometry(i);
      final CoordinatesList points = CoordinatesListUtil.get(subGeometry);
      writeXYCoordinates(out, points);
    }
  }

  public void writeZCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    if (coordinates.getNumAxis() >= 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getZ(i);
        if (Double.isNaN(z)) {
          out.writeLEDouble(0);
        } else {
          out.writeLEDouble(z);
        }
      }
    } else {
      for (int i = 0; i < coordinates.size(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeZCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeZCoordinates(out, coordinates);
    }
  }

  public void writeZCoordinates(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeZCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeZCoordinates(out, points);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (int n = 0; n < geometry.getNumGeometries(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
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
    if (minZ == Double.MAX_VALUE && maxZ == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
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
    if (minZ == Double.MAX_VALUE || maxZ == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

}
