package com.revolsys.gis.jts;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.GeometrySegmentQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.GeometryVertexQuadTree;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.jts.geom.vertex.Vertex;

public class GeometryEditUtil {

  private static final String GEOMETRY_SEGMENT_INDEX = "GeometrySegmentQuadTree";

  private static final String GEOMETRY_VERTEX_INDEX = "GeometryVertexQuadTree";

  private static final String POINT_QUAD_TREE = "PointQuadTree";

  static {
    GeometryEqualsExact3d.addExclude(GEOMETRY_SEGMENT_INDEX);
    GeometryEqualsExact3d.addExclude(POINT_QUAD_TREE);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T appendVertex(final Geometry geometry,
    final Point newPoint, final int[] geometryId) {
    if (geometry == null) {
      return (T)newPoint;
    } else if (newPoint == null) {
      return (T)geometry;
    } else if (newPoint.isEmpty()) {
      return (T)geometry;
    } else if (geometry.isEmpty()) {
      return (T)newPoint;
    } else {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        if (point.equals(2, newPoint)) {
          return (T)point;
        } else {
          final GeometryFactory geometryFactory = geometry.getGeometryFactory();
          return (T)geometryFactory.lineString(point, newPoint);
        }
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        return (T)appendVertex(line, newPoint);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = geometryId[0];
        return (T)appendVertex(polygon, ringIndex, newPoint);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        return (T)appendVertex(multiPoint, newPoint);
      } else {
        final int partIndex = geometryId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final List<Geometry> parts = geometry.getGeometries();
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            return (T)geometry;
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            newPart = appendVertex(line, newPoint);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = geometryId[1];
            newPart = appendVertex(polygon, ringIndex, newPoint);
          }
          parts.set(partIndex, newPart);
        }
      }
    }
    return (T)geometry;
  }

  protected static LineString appendVertex(
    final GeometryFactory geometryFactory, final LineString points,
    final Point newPoint) {
    final int axisCount = geometryFactory.getAxisCount();
    if (points == null) {
      return new LineStringDouble(axisCount);
    } else {
      final int vertexCount = points.getVertexCount();
      final double[] coordinates = new double[(vertexCount + 1) * axisCount];
      for (int i = 0; i < vertexCount; i++) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, i,
          points.getPoint(i));
      }
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount,
        newPoint);
      return new LineStringDouble(axisCount, coordinates);
    }
  }

  public static LineString appendVertex(final LineString line,
    final Point newPoint) {
    final int vertexCount = line.getVertexCount();
    final LineString newLine = insertVertex(line, vertexCount, newPoint);
    return newLine;
  }

  public static MultiPoint appendVertex(final MultiPoint multiPoint,
    final Point newPoint) {
    final List<Point> points = multiPoint.getPoints();
    final GeometryFactory geometryFactory = multiPoint.getGeometryFactory();
    points.add(newPoint);
    return geometryFactory.multiPoint(points);
  }

  public static Polygon appendVertex(final Polygon polygon,
    final int ringIndex, final Point newPoint) {
    if (ringIndex < 0 || ringIndex > polygon.getRingCount()
      || polygon.isEmpty()) {
      return polygon;
    } else {
      final List<LinearRing> rings = polygon.getRings();
      final LinearRing ring = rings.get(ringIndex);
      final GeometryFactory geometryFactory = polygon.getGeometryFactory();
      final LinearRing newPoints = insertVertex(ring,
        ring.getVertexCount() - 1, newPoint);

      rings.set(ringIndex, newPoints);
      return geometryFactory.polygon(rings);
    }
  }

  public static int[] createVertexIndex(final int[] index, final int vertexIndex) {
    final int length = index.length + 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, index.length);
    newIndex[index.length] = vertexIndex;
    return newIndex;
  }

  public static Geometry deleteVertex(final Geometry geometry,
    final int[] vertexId) {
    if (geometry != null && vertexId.length > 0) {
      final int pointIndex = vertexId[vertexId.length - 1];
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      if (geometry instanceof Point) {
        return geometry;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        return deleteVertex(line, pointIndex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        return deleteVertex(polygon, ringIndex, pointIndex);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final List<Geometry> parts = geometry.getGeometries();
          final Geometry part = parts.get(partIndex);
          if (part instanceof Point) {
            parts.remove(partIndex);
            return geometryFactory.geometry(parts);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final LineString newLine = deleteVertex(line, pointIndex);
            if (line != newLine) {
              parts.set(partIndex, newLine);
              return geometryFactory.geometry(parts);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = vertexId[1];
            final LinearRing ring = polygon.getRing(ringIndex);
            final LinearRing newRing = deleteVertex(ring, pointIndex);
            if (ring != newRing) {
              final List<LinearRing> rings = new ArrayList<>(polygon.getRings());
              rings.set(ringIndex, newRing);
              final Polygon newPart = geometryFactory.polygon(rings);
              parts.set(partIndex, newPart);
              return geometryFactory.geometry(parts);
            }
          }

        }
      }
    }
    return geometry;
  }

  public static LineString deleteVertex(final GeometryFactory geometryFactory,
    final LineString points, final int pointIndex) {
    final int vertexCount = points.getVertexCount();
    if (pointIndex >= 0 && pointIndex < vertexCount) {
      final List<Point> newPoints = new ArrayList<Point>();
      for (int i = 0; i < vertexCount; i++) {
        if (i != pointIndex) {
          newPoints.add(points.getPoint(i));
        }
      }
      return new LineStringDouble(geometryFactory.getAxisCount(), newPoints);
    }
    return points;
  }

  public static LinearRing deleteVertex(final LinearRing ring,
    final int pointIndex) {

    final GeometryFactory geometryFactory = ring.getGeometryFactory();
    final int vertexCount = ring.getVertexCount();
    final int axisCount = ring.getAxisCount();
    LineString newPoints;
    if (pointIndex == 0 || pointIndex == vertexCount - 1) {
      final double[] coordinates = new double[(vertexCount - 1) * axisCount];
      int i = 0;
      for (final Vertex vertex : ring.vertices()) {
        if (i > 0 && i < vertexCount - 1) {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, i - 1,
            vertex);
        }
        i++;
      }
      newPoints = new LineStringDouble(axisCount, coordinates);
    } else {
      newPoints = deleteVertex(geometryFactory, ring, pointIndex);
    }
    if (newPoints.getVertexCount() < 4) {
      return ring;
    } else {
      return geometryFactory.linearRing(newPoints);
    }
  }

  public static LineString deleteVertex(final LineString line,
    final int pointIndex) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final LineString newPoints = deleteVertex(geometryFactory, line, pointIndex);
    if (newPoints.getVertexCount() == 1) {
      return line;
    } else {
      return geometryFactory.lineString(newPoints);
    }
  }

  public static Polygon deleteVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex) {
    final LinearRing ring = polygon.getRing(ringIndex);
    final LinearRing newRing = deleteVertex(ring, pointIndex);
    if (ring == newRing) {
      return polygon;
    } else {
      final List<LinearRing> rings = new ArrayList<>(polygon.getRings());
      rings.set(ringIndex, newRing);
      final GeometryFactory geometryFactory = polygon.getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  public static Point getCoordinatesOffset(final Geometry geometry,
    final int[] vertexId, final int offset) {
    final int[] newPointId = vertexId.clone();
    newPointId[vertexId.length - 1] = newPointId[vertexId.length - 1] + offset;
    return geometry.getVertex(newPointId);
  }

  public static GeometrySegmentQuadTree getGeometrySegmentIndex(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<GeometrySegmentQuadTree> reference = GeometryProperties.getGeometryProperty(
        geometry, GEOMETRY_SEGMENT_INDEX);
      GeometrySegmentQuadTree index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        index = new GeometrySegmentQuadTree(geometry);
        GeometryProperties.setGeometryProperty(geometry,
          GEOMETRY_SEGMENT_INDEX, new SoftReference<GeometrySegmentQuadTree>(
            index));
      }
      return index;
    }
    return new GeometrySegmentQuadTree(null);
  }

  public static GeometryVertexQuadTree getGeometryVertexIndex(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<GeometryVertexQuadTree> reference = GeometryProperties.getGeometryProperty(
        geometry, GEOMETRY_VERTEX_INDEX);
      GeometryVertexQuadTree index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        index = new GeometryVertexQuadTree(geometry);
        GeometryProperties.setGeometryProperty(geometry,
          GEOMETRY_SEGMENT_INDEX, new SoftReference<GeometryVertexQuadTree>(
            index));
      }
      return index;
    }
    return new GeometryVertexQuadTree(null);
  }

  public static Map<int[], Point> getIndexOfVertices(final Geometry geometry) {
    final Map<int[], Point> pointIndexes = new LinkedHashMap<int[], Point>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      for (final Vertex vertex : geometry.vertices()) {
        final int[] vertexId = vertex.getVertexId();
        final Vertex clone = vertex.clone();
        pointIndexes.put(vertexId, clone);
      }
    }
    return pointIndexes;
  }

  public static PointQuadTree<int[]> getPointQuadTree(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return new PointQuadTree<int[]>();
    } else {
      final Reference<PointQuadTree<int[]>> reference = GeometryProperties.getGeometryProperty(
        geometry, POINT_QUAD_TREE);
      PointQuadTree<int[]> index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {

        final GeometryFactory geometryFactory = geometry.getGeometryFactory();
        index = new PointQuadTree<int[]>(geometryFactory);
        for (final Vertex vertex : geometry.vertices()) {
          final double x = vertex.getX();
          final double y = vertex.getY();
          final int[] vertexId = vertex.getVertexId();
          index.put(x, y, vertexId);
        }
        GeometryProperties.setGeometryProperty(geometry, POINT_QUAD_TREE,
          new SoftReference<PointQuadTree<int[]>>(index));
      }
      return index;
    }
  }

  public static Point getVertex(final Geometry geometry, final int[] partId,
    final int pointIndex) {
    final int[] vertexId = new int[partId.length + 1];
    System.arraycopy(partId, 0, vertexId, 0, partId.length);
    vertexId[partId.length] = pointIndex;
    return geometry.getVertex(vertexId);
  }

  public static int getVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    return index[lastIndex];
  }

  public static int[] incrementVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, lastIndex);
    newIndex[lastIndex] = index[lastIndex] + 1;
    return newIndex;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Point newPoint, final int[] vertexId) {
    if (geometry != null && newPoint != null) {
      if (geometry instanceof Point) {
        return (T)geometry;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final int pointIndex = vertexId[0];
        return (T)insertVertex(line, pointIndex, newPoint);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        final int pointIndex = vertexId[1];
        return (T)insertVertex(polygon, ringIndex, pointIndex, newPoint);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        final int pointIndex = vertexId[0];
        return (T)insertVertex(multiPoint, pointIndex, newPoint);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final List<Geometry> parts = geometry.getGeometries();
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            return (T)geometry;
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final int pointIndex = vertexId[1];
            newPart = insertVertex(line, pointIndex, newPoint);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = vertexId[1];
            final int pointIndex = vertexId[2];
            newPart = insertVertex(polygon, ringIndex, pointIndex, newPoint);
          }
          parts.set(partIndex, newPart);
        }
      }
    }
    return (T)geometry;
  }

  public static LineString insertVertex(final GeometryFactory geometryFactory,
    final LineString points, final int pointIndex, final Point newPoint) {
    final List<Point> newPoints = new ArrayList<Point>();
    for (int i = 0; i < points.getVertexCount(); i++) {
      newPoints.add(points.getPoint(i));
    }
    newPoints.add(pointIndex, newPoint);
    return new LineStringDouble(geometryFactory.getAxisCount(), newPoints);
  }

  public static LineString insertVertex(final GeometryFactory geometryFactory,
    final List<Point> points, final int pointIndex, final Point newPoint) {
    final List<Point> newPoints = new ArrayList<Point>();
    for (int i = 0; i < points.size(); i++) {
      newPoints.add(points.get(i));
    }
    newPoints.add(pointIndex, newPoint);
    return new LineStringDouble(geometryFactory.getAxisCount(), newPoints);
  }

  public static LinearRing insertVertex(final LinearRing line,
    final int pointIndex, final Point newPoint) {
    final int axisCount = line.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = new double[axisCount * (vertexCount + 1)];
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, line, 0,
      pointIndex);
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex,
      newPoint);
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex + 1,
      line, pointIndex, vertexCount - pointIndex);

    final GeometryFactory geometryFactory = line.getGeometryFactory();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  public static LineString insertVertex(final LineString line,
    final int pointIndex, final Point newPoint) {
    final int axisCount = line.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = new double[axisCount * (vertexCount + 1)];
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, line, 0,
      pointIndex);
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex,
      newPoint);
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex + 1,
      line, pointIndex, vertexCount - pointIndex);

    final GeometryFactory geometryFactory = line.getGeometryFactory();
    return geometryFactory.lineString(axisCount, coordinates);
  }

  public static MultiPoint insertVertex(final MultiPoint multiPoint,
    final int pointIndex, final Point newPoint) {
    final List<Point> coordinatesList = multiPoint.getPoints();
    final GeometryFactory geometryFactory = multiPoint.getGeometryFactory();
    final LineString points = insertVertex(geometryFactory, coordinatesList,
      pointIndex, newPoint);
    return geometryFactory.multiPoint(points);
  }

  public static Polygon insertVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex, final Point newPoint) {
    final List<LinearRing> rings = polygon.getRings();
    LinearRing ring = rings.get(ringIndex);

    ring = insertVertex(ring, pointIndex, newPoint);

    rings.set(ringIndex, ring);
    final GeometryFactory geometryFactory = polygon.getGeometryFactory();
    return geometryFactory.polygon(rings);
  }

  public static boolean isFromPoint(final Geometry geometry,
    final int[] vertexId) {
    if (geometry != null) {
      final Vertex vertex = geometry.getVertex(vertexId);
      if (vertex != null) {
        return vertex.isFrom();
      }
    }
    return false;
  }

  public static boolean isToPoint(final Geometry geometry, final int[] vertexId) {
    if (geometry != null) {
      final Vertex vertex = geometry.getVertex(vertexId);
      if (vertex != null) {
        return vertex.isTo();
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T moveVertex(final Geometry geometry,
    final Point newPoint, final int[] vertexId) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      if (geometry instanceof Point) {
        return (T)geometryFactory.point(newPoint);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final int pointIndex = vertexId[0];
        return (T)moveVertex(line, pointIndex, newPoint);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        final int pointIndex = vertexId[1];
        return (T)moveVertex(polygon, ringIndex, pointIndex, newPoint);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        final int pointIndex = vertexId[0];
        return (T)moveVertex(multiPoint, pointIndex, newPoint);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final List<Geometry> parts = geometry.getGeometries();
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            newPart = geometryFactory.point(newPoint);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final int pointIndex = vertexId[1];
            newPart = moveVertex(line, pointIndex, newPoint);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = vertexId[1];
            final int pointIndex = vertexId[2];
            newPart = moveVertex(polygon, ringIndex, pointIndex, newPoint);
          }
          parts.set(partIndex, newPart);
          return (T)geometryFactory.geometry(parts);
        }
      }
    }
    return (T)geometry;
  }

  public static LinearRing moveVertex(final LinearRing ring, int pointIndex,
    final Point newPoint) {
    if (ring == null || ring.isEmpty()) {
      return ring;
    } else {
      final int vertexCount = ring.getVertexCount();
      while (pointIndex < 0) {
        pointIndex += vertexCount;
      }
      if (pointIndex >= vertexCount) {
        return ring;
      } else {
        final double[] coordinates = ring.getCoordinates();
        final int axisCount = ring.getAxisCount();
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex,
          newPoint);
        if (pointIndex == 0) {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount,
            vertexCount - 1, newPoint);
        } else if (pointIndex == vertexCount - 1) {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0,
            newPoint);
        }
        final GeometryFactory geometryFactory = ring.getGeometryFactory();
        return geometryFactory.linearRing(axisCount, coordinates);
      }
    }
  }

  public static LineString moveVertex(final LineString line, int pointIndex,
    final Point newPoint) {
    if (line == null || line.isEmpty()) {
      return line;
    } else {
      final int vertexCount = line.getVertexCount();
      while (pointIndex < 0) {
        pointIndex += vertexCount;
      }
      if (pointIndex >= vertexCount) {
        return line;
      } else {
        final double[] coordinates = line.getCoordinates();
        final int axisCount = line.getAxisCount();
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, pointIndex,
          newPoint);
        final GeometryFactory geometryFactory = line.getGeometryFactory();
        return geometryFactory.lineString(axisCount, coordinates);
      }
    }
  }

  public static MultiPoint moveVertex(final MultiPoint multiPoint,
    int pointIndex, final Point newPoint) {
    if (multiPoint == null || multiPoint.isEmpty()) {
      return multiPoint;
    } else {
      final int vertexCount = multiPoint.getGeometryCount();
      while (pointIndex < 0) {
        pointIndex += vertexCount;
      }
      if (pointIndex >= vertexCount) {
        return multiPoint;
      } else {
        final GeometryFactory geometryFactory = multiPoint.getGeometryFactory();
        final Point point = geometryFactory.point(newPoint);
        final List<Point> points = new ArrayList<>(multiPoint.getPoints());
        points.set(pointIndex, point);
        return geometryFactory.multiPoint(points);
      }
    }
  }

  public static Polygon moveVertex(final Polygon polygon, final int ringIndex,
    final int pointIndex, final Point newPoint) {
    final LinearRing ring = polygon.getRing(ringIndex);
    final LinearRing newRing = moveVertex(ring, pointIndex, newPoint);
    if (ring == newRing) {
      return polygon;
    } else {
      final List<LinearRing> rings = new ArrayList<>(polygon.getRings());
      rings.set(ringIndex, newRing);
      final GeometryFactory geometryFactory = polygon.getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G moveVertexIfEqual(final G geometry,
    final Point originalLocation, final Point newLocation,
    final int... vertexId) {
    final Point coordinates = geometry.getVertex(vertexId);
    CoordinatesUtil.setElevation(newLocation, originalLocation);
    if (coordinates.equals(2, originalLocation)) {
      final Point newCoordinates = CoordinatesUtil.setElevation(newLocation,
        originalLocation);
      return (G)moveVertex(geometry, newCoordinates, vertexId);
    } else {
      return geometry;
    }

  }

  public static int[] setVertexIndex(final int[] index, final int vertexIndex) {
    final int length = index.length;
    final int lastIndex = length - 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, lastIndex);
    newIndex[lastIndex] = vertexIndex;
    return newIndex;
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G toCounterClockwise(final G geometry) {
    if (geometry instanceof Point) {
      return geometry;
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isCounterClockwise()) {
        return geometry;
      } else {
        return (G)line.reverse();
      }
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      boolean changed = false;
      final List<LinearRing> rings = new ArrayList<>();
      int i = 0;
      for (final LinearRing ring : polygon.rings()) {
        final boolean counterClockwise = ring.isCounterClockwise();
        boolean pointsChanged;
        if (i == 0) {
          pointsChanged = !counterClockwise;
        } else {
          pointsChanged = counterClockwise;
        }
        if (pointsChanged) {
          changed = true;
          final LinearRing reverse = ring.reverse();
          rings.add(reverse);
        } else {
          rings.add(ring);
        }
        i++;
      }
      if (changed) {
        return (G)geometry.getGeometryFactory().polygon(rings);
      } else {
        return geometry;
      }
    } else {
      return geometry;
    }
  }

}
