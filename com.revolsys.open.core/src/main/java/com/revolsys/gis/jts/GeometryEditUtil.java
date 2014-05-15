package com.revolsys.gis.jts;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.linesegment.LineSegmentQuadTree;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.data.equals.GeometryEqualsExact3d;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;

public class GeometryEditUtil {

  private static final String LINE_SEGMENT_QUAD_TREE = "LineSegmentQuadTree";

  private static final String POINT_QUAD_TREE = "PointQuadTree";

  static {
    GeometryEqualsExact3d.addExclude(LINE_SEGMENT_QUAD_TREE);
    GeometryEqualsExact3d.addExclude(POINT_QUAD_TREE);
  }

  protected static void add(final Map<int[], Point> pointIndexes,
    final Geometry geometry) {
    final PointList points = CoordinatesListUtil.get(geometry);
    for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
      final Point point = points.get(pointIndex).cloneCoordinates();
      add(pointIndexes, point, pointIndex);
    }
  }

  protected static void add(final Map<int[], Point> pointIndexes,
    final Point point, final int... index) {
    pointIndexes.put(index, point);
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

  protected static PointList appendVertex(
    final GeometryFactory geometryFactory, final PointList points,
    final Point newPoint) {
    final int axisCount = geometryFactory.getAxisCount();
    if (points == null) {
      return new DoubleCoordinatesList(axisCount);
    } else {
      final int vertexCount = points.size();
      final double[] coordinates = new double[(vertexCount + 1) * axisCount];
      for (int i = 0; i < vertexCount; i++) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, i,
          points.get(i));
      }
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount,
        newPoint);
      return new DoubleCoordinatesList(axisCount, coordinates);
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
    final PointList points = CoordinatesListUtil.get(multiPoint);
    final GeometryFactory geometryFactory = multiPoint.getGeometryFactory();
    final PointList newPoints = appendVertex(geometryFactory, points, newPoint);
    return geometryFactory.multiPoint(newPoints);
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

  public static PointList deleteVertex(final GeometryFactory geometryFactory,
    final LineString points, final int pointIndex) {
    final int vertexCount = points.getVertexCount();
    if (pointIndex >= 0 && pointIndex < vertexCount) {
      final List<Point> newPoints = new ArrayList<Point>();
      for (int i = 0; i < vertexCount; i++) {
        if (i != pointIndex) {
          newPoints.add(points.getPoint(i));
        }
      }
      return new DoubleCoordinatesList(geometryFactory.getAxisCount(),
        newPoints);
    }
    return points.getCoordinatesList();
  }

  public static LinearRing deleteVertex(final LinearRing ring,
    final int pointIndex) {

    final GeometryFactory geometryFactory = ring.getGeometryFactory();
    final int vertexCount = ring.getVertexCount();
    final int axisCount = ring.getAxisCount();
    PointList newPoints;
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
      newPoints = new DoubleCoordinatesList(axisCount, coordinates);
    } else {
      newPoints = deleteVertex(geometryFactory, ring, pointIndex);
    }
    if (newPoints.size() < 4) {
      return ring;
    } else {
      return geometryFactory.linearRing(newPoints);
    }
  }

  public static LineString deleteVertex(final LineString line,
    final int pointIndex) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final PointList newPoints = deleteVertex(geometryFactory, line, pointIndex);
    if (newPoints.size() == 1) {
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
    return getVertex(geometry, newPointId);
  }

  public static Map<int[], Point> getIndexOfVertices(final Geometry geometry) {
    final Map<int[], Point> pointIndexes = new LinkedHashMap<int[], Point>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        pointIndexes.put(new int[] {
          0
        }, point);
      } else if (geometry instanceof LineString) {
        add(pointIndexes, geometry);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
        for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
          final PointList points = rings.get(ringIndex);
          for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            final Point point = points.get(pointIndex).cloneCoordinates();
            add(pointIndexes, point, ringIndex, pointIndex);
          }
        }
      } else if (geometry instanceof MultiPoint) {
        add(pointIndexes, geometry);
      } else {
        for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
          final Geometry part = geometry.getGeometry(partIndex);
          if (part instanceof Point) {
            final Point point = (Point)part;
            add(pointIndexes, point, partIndex, 0);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final PointList points = CoordinatesListUtil.get(line);
            for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
              final Point point = points.get(pointIndex).cloneCoordinates();
              add(pointIndexes, point, partIndex, pointIndex);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
            for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
              final PointList points = rings.get(ringIndex);
              for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
                final Point point = points.get(pointIndex).cloneCoordinates();
                add(pointIndexes, point, partIndex, ringIndex, pointIndex);
              }
            }
          }
        }
      }
    }
    return pointIndexes;
  }

  public static LineSegmentQuadTree getLineSegmentQuadTree(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<LineSegmentQuadTree> reference = GeometryProperties.getGeometryProperty(
        geometry, LINE_SEGMENT_QUAD_TREE);
      LineSegmentQuadTree index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        index = new LineSegmentQuadTree(geometry);
        GeometryProperties.setGeometryProperty(geometry,
          LINE_SEGMENT_QUAD_TREE, new SoftReference<LineSegmentQuadTree>(index));
      }
      return index;
    }
    return new LineSegmentQuadTree(null);
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

        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          index.put(point, new int[] {
            0
          });
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          final PointList points = CoordinatesListUtil.get(line);
          for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            final double x = points.getX(pointIndex);
            final double y = points.getY(pointIndex);
            index.put(x, y, new int[] {
              pointIndex
            });
          }
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
          for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
            final PointList points = rings.get(ringIndex);
            for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
              final double x = points.getX(pointIndex);
              final double y = points.getY(pointIndex);
              index.put(x, y, new int[] {
                ringIndex, pointIndex
              });
            }
          }
        } else {
          for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
            final Geometry part = geometry.getGeometry(partIndex);
            if (part instanceof Point) {
              final Point point = (Point)part;
              index.put(point, new int[] {
                partIndex, 0
              });
            } else if (part instanceof LineString) {
              final LineString line = (LineString)part;
              final PointList points = CoordinatesListUtil.get(line);
              for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
                final double x = points.getX(pointIndex);
                final double y = points.getY(pointIndex);
                index.put(x, y, new int[] {
                  partIndex, pointIndex
                });
              }
            } else if (part instanceof Polygon) {
              final Polygon polygon = (Polygon)part;
              final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
              for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
                final PointList points = rings.get(ringIndex);
                for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
                  final double x = points.getX(pointIndex);
                  final double y = points.getY(pointIndex);
                  index.put(x, y, new int[] {
                    partIndex, ringIndex, pointIndex
                  });
                }
              }
            }
          }
        }
        GeometryProperties.setGeometryProperty(geometry, POINT_QUAD_TREE,
          new SoftReference<PointQuadTree<int[]>>(index));
      }
      return index;
    }
  }

  public static PointList getPoints(final Geometry geometry,
    final int[] vertexId) {
    if (geometry == null) {
      return null;
    } else {
      if (geometry instanceof Point) {
        final PointList points = CoordinatesListUtil.get(geometry);
        return points;
      } else if (geometry instanceof LineString) {
        final PointList points = CoordinatesListUtil.get(geometry);
        return points;
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
        final int ringIndex = vertexId[0];

        final PointList points = rings.get(ringIndex);
        return points;
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final Geometry part = geometry.getGeometry(partIndex);
          if (part instanceof Point) {
            final PointList points = CoordinatesListUtil.get(part);
            return points;
          } else if (part instanceof LineString) {
            final PointList points = CoordinatesListUtil.get(part);
            return points;
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
            final int vertexIndex = vertexId[1];
            final PointList points = rings.get(vertexIndex);
            return points;
          }
        }
      }
      return null;
    }
  }

  public static Point getVertex(final Geometry geometry, final int[] vertexId) {
    if (geometry == null || vertexId == null || vertexId.length == 0) {
      return null;
    } else {
      int pointIndex = vertexId[vertexId.length - 1];
      if (geometry instanceof Point) {
        if (pointIndex == 0 || pointIndex == -1) {
          final Point point = (Point)geometry;
          return point;
        } else {
          return null;
        }
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final PointList points = CoordinatesListUtil.get(line);
        final int numPoints = points.size();
        while (numPoints > 0 && pointIndex < 0) {
          pointIndex += numPoints;
        }
        pointIndex = pointIndex % numPoints;
        return getVertex(points, pointIndex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
        final int ringIndex = vertexId[0];

        final PointList points = rings.get(ringIndex);
        final int numPoints = points.size();
        while (numPoints > 0 && pointIndex < 0) {
          pointIndex += numPoints - 1;
        }
        return getVertex(points, pointIndex);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final Geometry part = geometry.getGeometry(partIndex);
          if (part instanceof Point) {
            if (pointIndex == 0) {
              final Point point = (Point)part;
              return point;
            } else {
              return null;
            }
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final PointList points = CoordinatesListUtil.get(line);
            final int numPoints = points.size();
            while (numPoints > 0 && pointIndex < 0) {
              pointIndex += numPoints;
            }
            return getVertex(points, pointIndex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
            final int vertexIndex = vertexId[1];
            final PointList points = rings.get(vertexIndex);
            return getVertex(points, pointIndex);
          }
        }
      }
      return null;
    }
  }

  public static Point getVertex(final Geometry geometry, final int[] partId,
    final int pointIndex) {
    final int[] vertexId = new int[partId.length + 1];
    System.arraycopy(partId, 0, vertexId, 0, partId.length);
    vertexId[partId.length] = pointIndex;
    return getVertex(geometry, vertexId);
  }

  public static Point getVertex(final PointList points, final int pointIndex) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      final Point point = points.get(pointIndex);
      if (point != null) {
        return point.cloneCoordinates();
      }
    }
    return null;
  }

  public static int getVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    return index[lastIndex];
  }

  public static Point getVertexPoint(final Geometry geometry,
    final int[] vertexId) {
    final Point vertex = getVertex(geometry, vertexId);
    if (vertex == null) {
      return null;
    } else {
      return geometry.getGeometryFactory().point(vertex);
    }
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

  public static PointList insertVertex(final GeometryFactory geometryFactory,
    final PointList points, final int pointIndex, final Point newPoint) {
    final List<Point> newPoints = new ArrayList<Point>();
    for (int i = 0; i < points.size(); i++) {
      newPoints.add(points.getCoordinate(i));
    }
    newPoints.add(pointIndex, newPoint);
    return new DoubleCoordinatesList(geometryFactory.getAxisCount(), newPoints);
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
      line, 0, vertexCount - pointIndex);

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
      line, 0, vertexCount - pointIndex);

    final GeometryFactory geometryFactory = line.getGeometryFactory();
    return geometryFactory.lineString(axisCount, coordinates);
  }

  public static MultiPoint insertVertex(final MultiPoint multiPoint,
    final int pointIndex, final Point newPoint) {
    final PointList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final GeometryFactory geometryFactory = multiPoint.getGeometryFactory();
    final PointList points = insertVertex(geometryFactory, coordinatesList,
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
    final PointList points = getPoints(geometry, vertexId);
    if (points != null && points.size() > 0 && vertexId.length > 0) {
      final int index = vertexId[vertexId.length - 1];
      return index == 0;
    }
    return false;
  }

  public static boolean isToPoint(final Geometry geometry, final int[] vertexId) {
    final PointList points = getPoints(geometry, vertexId);
    if (points != null && points.size() > 0 && vertexId.length > 0) {
      final int index = vertexId[vertexId.length - 1];
      return index == points.size() - 1;
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
        final double[] coordinates = ring.getCoordinatesList().getCoordinates();
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
        final double[] coordinates = line.getCoordinatesList().getCoordinates();
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
    final Point coordinates = getVertex(geometry, vertexId);
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
