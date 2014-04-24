package com.revolsys.gis.jts;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.data.equals.GeometryEqualsExact3d;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class GeometryEditUtil {

  private static final String LINE_SEGMENT_QUAD_TREE = "LineSegmentQuadTree";

  private static final String POINT_QUAD_TREE = "PointQuadTree";

  static {
    GeometryEqualsExact3d.addExclude(LINE_SEGMENT_QUAD_TREE);
    GeometryEqualsExact3d.addExclude(POINT_QUAD_TREE);
  }

  protected static void add(final Map<int[], Coordinates> pointIndexes,
    final Coordinates point, final int... index) {
    pointIndexes.put(index, point);
  }

  protected static void add(final Map<int[], Coordinates> pointIndexes,
    final Geometry geometry) {
    final CoordinatesList points = CoordinatesListUtil.get(geometry);
    for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
      final Coordinates point = points.get(pointIndex).cloneCoordinates();
      add(pointIndexes, point, pointIndex);
    }
  }

  protected static CoordinatesList appendVertex(final CoordinatesList points,
    final Coordinates newPoint) {
    if (points == null) {
      return new ListCoordinatesList(points);
    } else {
      final int size = points.size();
      final int axisCount = points.getAxisCount();
      final CoordinatesList newPoints = new DoubleCoordinatesList(size + 1,
        axisCount);
      points.copy(0, newPoints, 0, axisCount, size);
      newPoints.setPoint(size, newPoint);
      return newPoints;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T appendVertex(final Geometry geometry,
    final Coordinates newPoint, final int[] geometryId) {
    if (geometry != null && newPoint != null) {
      if (geometry instanceof Point) {
        return (T)geometry;
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

  public static <T extends Geometry> T appendVertex(final Geometry geometry,
    final Point newPoint, final int[] partId) {
    return appendVertex(geometry, CoordinatesUtil.getInstance(newPoint), partId);
  }

  public static LineString appendVertex(final LineString line,
    final Coordinates newPoint) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesList newPoints = appendVertex(points, newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.lineString(newPoints);
  }

  public static MultiPoint appendVertex(final MultiPoint multiPoint,
    final Coordinates newPoint) {
    final CoordinatesList points = CoordinatesListUtil.get(multiPoint);
    final CoordinatesList newPoints = appendVertex(points, newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.multiPoint(newPoints);
  }

  public static Polygon appendVertex(final Polygon polygon,
    final int ringIndex, final Coordinates newPoint) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList points = rings.get(ringIndex);
    final CoordinatesList newPoints = insertVertex(points, points.size() - 1,
      newPoint);

    rings.set(ringIndex, newPoints);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.polygon(rings);
  }

  public static int[] createVertexIndex(final int[] index, final int vertexIndex) {
    final int length = index.length + 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, index.length);
    newIndex[index.length] = vertexIndex;
    return newIndex;
  }

  public static CoordinatesList deleteVertex(final CoordinatesList points,
    final int pointIndex) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      final ListCoordinatesList newPoints = new ListCoordinatesList(points);
      newPoints.remove(pointIndex);
      return newPoints;
    }
    return points;
  }

  public static Geometry deleteVertex(final Geometry geometry,
    final int[] vertexId) {
    if (geometry != null && vertexId.length > 0) {
      final int pointIndex = vertexId[vertexId.length - 1];
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
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
            final Geometry newLine = deleteVertex(line, pointIndex);
            if (line != newLine) {
              parts.set(partIndex, newLine);
              return geometryFactory.geometry(parts);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int ringIndex = vertexId[1];
            final CoordinatesList points = rings.get(ringIndex);
            final CoordinatesList newPoints = deleteVertex(points, pointIndex);
            if (newPoints != points) {
              rings.set(ringIndex, newPoints);
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

  public static LineString deleteVertex(final LineString line,
    final int pointIndex) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesList newPoints = deleteVertex(points, pointIndex);
    if (newPoints != points) {
      if (newPoints.size() == 1) {
        return line;
      } else {
        return geometryFactory.lineString(newPoints);
      }
    } else {
      return line;
    }
  }

  public static Polygon deleteVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList points = rings.get(ringIndex).clone();
    CoordinatesList newPoints;
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    if (pointIndex == 0 || pointIndex == points.size() - 1) {
      newPoints = points.subList(1, points.size() - 1);
      final Coordinates firstPoint = newPoints.get(0);
      newPoints.setPoint(newPoints.size() - 1, firstPoint);
    } else {
      newPoints = deleteVertex(points, pointIndex);
    }
    if (newPoints == points) {
      return polygon;
    } else if (newPoints.size() < 4) {
      return polygon;
    } else {
      rings.set(ringIndex, newPoints);
      return geometryFactory.polygon(rings);
    }
  }

  public static Coordinates getCoordinatesOffset(final Geometry geometry,
    final int[] vertexId, final int offset) {
    final int[] newPointId = vertexId.clone();
    newPointId[vertexId.length - 1] = newPointId[vertexId.length - 1] + offset;
    return getVertex(geometry, newPointId);
  }

  public static Map<int[], Coordinates> getIndexOfVertices(
    final Geometry geometry) {
    final Map<int[], Coordinates> pointIndexes = new LinkedHashMap<int[], Coordinates>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        final Coordinates coordinates = CoordinatesUtil.getInstance(point);
        pointIndexes.put(new int[] {
          0
        }, coordinates);
      } else if (geometry instanceof LineString) {
        add(pointIndexes, geometry);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
          final CoordinatesList points = rings.get(ringIndex);
          for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            final Coordinates point = points.get(pointIndex).cloneCoordinates();
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
            final Coordinates coordinates = CoordinatesUtil.getInstance(point);
            add(pointIndexes, coordinates, partIndex, 0);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final CoordinatesList points = CoordinatesListUtil.get(line);
            for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
              final Coordinates point = points.get(pointIndex)
                .cloneCoordinates();
              add(pointIndexes, point, partIndex, pointIndex);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
              final CoordinatesList points = rings.get(ringIndex);
              for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
                final Coordinates point = points.get(pointIndex)
                  .cloneCoordinates();
                add(pointIndexes, point, partIndex, ringIndex, pointIndex);
              }
            }
          }
        }
      }
    }
    return pointIndexes;
  }

  public static QuadTree<IndexedLineSegment> getLineSegmentQuadTree(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<QuadTree<IndexedLineSegment>> reference = GeometryProperties.getGeometryProperty(
        geometry, LINE_SEGMENT_QUAD_TREE);
      QuadTree<IndexedLineSegment> index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
        index = new QuadTree<IndexedLineSegment>(geometryFactory);
        if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          int segmentIndex = 0;
          for (final LineSegment lineSegment : new CoordinatesListIndexLineSegmentIterator(
            line)) {
            final IndexedLineSegment indexedSegment = new IndexedLineSegment(
              geometryFactory, lineSegment, segmentIndex);
            index.insert(indexedSegment.getBoundingBox(), indexedSegment);
            segmentIndex++;
          }
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
          for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
            final CoordinatesList ring = rings.get(ringIndex);
            int segmentIndex = 0;
            for (final LineSegment lineSegment : new CoordinatesListIndexLineSegmentIterator(
              ring)) {
              final IndexedLineSegment indexedSegment = new IndexedLineSegment(
                geometryFactory, lineSegment, ringIndex, segmentIndex);
              index.insert(indexedSegment.getBoundingBox(), indexedSegment);
              segmentIndex++;
            }
          }
        } else {
          for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
            final Geometry part = geometry.getGeometry(partIndex);
            if (part instanceof LineString) {
              final LineString line = (LineString)part;
              int segmentIndex = 0;
              for (final LineSegment lineSegment : new CoordinatesListIndexLineSegmentIterator(
                line)) {
                final IndexedLineSegment indexedSegment = new IndexedLineSegment(
                  geometryFactory, lineSegment, partIndex, segmentIndex);
                index.insert(indexedSegment.getBoundingBox(), indexedSegment);
                segmentIndex++;
              }
            } else if (part instanceof Polygon) {
              final Polygon polygon = (Polygon)part;
              final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
              for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
                final CoordinatesList ring = rings.get(ringIndex);
                int segmentIndex = 0;
                for (final LineSegment lineSegment : new CoordinatesListIndexLineSegmentIterator(
                  ring)) {
                  final IndexedLineSegment indexedSegment = new IndexedLineSegment(
                    geometryFactory, lineSegment, partIndex, ringIndex,
                    segmentIndex);
                  index.insert(indexedSegment.getBoundingBox(), indexedSegment);
                  segmentIndex++;
                }
              }
            }
          }
        }
        GeometryProperties.setGeometryProperty(geometry,
          LINE_SEGMENT_QUAD_TREE,
          new SoftReference<QuadTree<IndexedLineSegment>>(index));
      }
      return index;
    }
    return new QuadTree<IndexedLineSegment>();
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
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
        index = new PointQuadTree<int[]>(geometryFactory);

        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          final Coordinates coordinates = CoordinatesUtil.getInstance(point);
          index.put(coordinates, new int[] {
            0
          });
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          final CoordinatesList points = CoordinatesListUtil.get(line);
          for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            final double x = points.getX(pointIndex);
            final double y = points.getY(pointIndex);
            index.put(x, y, new int[] {
              pointIndex
            });
          }
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
          for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
            final CoordinatesList points = rings.get(ringIndex);
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
              final Coordinates coordinates = CoordinatesUtil.getInstance(point);
              index.put(coordinates, new int[] {
                partIndex, 0
              });
            } else if (part instanceof LineString) {
              final LineString line = (LineString)part;
              final CoordinatesList points = CoordinatesListUtil.get(line);
              for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
                final double x = points.getX(pointIndex);
                final double y = points.getY(pointIndex);
                index.put(x, y, new int[] {
                  partIndex, pointIndex
                });
              }
            } else if (part instanceof Polygon) {
              final Polygon polygon = (Polygon)part;
              final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
              for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
                final CoordinatesList points = rings.get(ringIndex);
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

  public static CoordinatesList getPoints(final Geometry geometry,
    final int[] vertexId) {
    if (geometry == null) {
      return null;
    } else {
      if (geometry instanceof Point) {
        final CoordinatesList points = CoordinatesListUtil.get(geometry);
        return points;
      } else if (geometry instanceof LineString) {
        final CoordinatesList points = CoordinatesListUtil.get(geometry);
        return points;
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        final int ringIndex = vertexId[0];

        final CoordinatesList points = rings.get(ringIndex);
        return points;
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getGeometryCount()) {
          final Geometry part = geometry.getGeometry(partIndex);
          if (part instanceof Point) {
            final CoordinatesList points = CoordinatesListUtil.get(part);
            return points;
          } else if (part instanceof LineString) {
            final CoordinatesList points = CoordinatesListUtil.get(part);
            return points;
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int vertexIndex = vertexId[1];
            final CoordinatesList points = rings.get(vertexIndex);
            return points;
          }
        }
      }
      return null;
    }
  }

  public static Coordinates getVertex(final CoordinatesList points,
    final int pointIndex) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      final Coordinates point = points.get(pointIndex);
      if (point != null) {
        return point.cloneCoordinates();
      }
    }
    return null;
  }

  public static Coordinates getVertex(final Geometry geometry,
    final int[] vertexId) {
    if (geometry == null || vertexId == null || vertexId.length == 0) {
      return null;
    } else {
      int pointIndex = vertexId[vertexId.length - 1];
      if (geometry instanceof Point) {
        if (pointIndex == 0) {
          final Point point = (Point)geometry;
          final Coordinates coordinates = CoordinatesUtil.getInstance(point);
          return coordinates;
        } else {
          return null;
        }
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final CoordinatesList points = CoordinatesListUtil.get(line);
        final int numPoints = points.size();
        while (numPoints > 0 && pointIndex < 0) {
          pointIndex += numPoints;
        }
        pointIndex = pointIndex % numPoints;
        return getVertex(points, pointIndex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        final int ringIndex = vertexId[0];

        final CoordinatesList points = rings.get(ringIndex);
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
              final Coordinates coordinates = CoordinatesUtil.getInstance(point);
              return coordinates;
            } else {
              return null;
            }
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final CoordinatesList points = CoordinatesListUtil.get(line);
            final int numPoints = points.size();
            while (numPoints > 0 && pointIndex < 0) {
              pointIndex += numPoints;
            }
            return getVertex(points, pointIndex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int vertexIndex = vertexId[1];
            final CoordinatesList points = rings.get(vertexIndex);
            return getVertex(points, pointIndex);
          }
        }
      }
      return null;
    }
  }

  public static Coordinates getVertex(final Geometry geometry,
    final int[] partId, final int pointIndex) {
    final int[] vertexId = new int[partId.length + 1];
    System.arraycopy(partId, 0, vertexId, 0, partId.length);
    vertexId[partId.length] = pointIndex;
    return getVertex(geometry, vertexId);
  }

  public static int getVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    return index[lastIndex];
  }

  public static Point getVertexPoint(final Geometry geometry,
    final int[] vertexId) {
    final Coordinates vertex = getVertex(geometry, vertexId);
    if (vertex == null) {
      return null;
    } else {
      return GeometryFactory.getFactory(geometry).point(vertex);
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

  protected static ListCoordinatesList insertVertex(
    final CoordinatesList coordinatesList, final int pointIndex,
    final Coordinates newPoint) {
    final ListCoordinatesList points = new ListCoordinatesList(coordinatesList);
    points.add(pointIndex, newPoint);
    return points;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Coordinates newPoint, final int[] vertexId) {
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

  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Point newPoint, final int[] vertexId) {
    return insertVertex(geometry, CoordinatesUtil.getInstance(newPoint),
      vertexId);
  }

  public static LineString insertVertex(final LineString line,
    final int pointIndex, final Coordinates newPoint) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final ListCoordinatesList points = insertVertex(coordinatesList,
      pointIndex, newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.lineString(points);
  }

  public static MultiPoint insertVertex(final MultiPoint multiPoint,
    final int pointIndex, final Coordinates newPoint) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final ListCoordinatesList points = insertVertex(coordinatesList,
      pointIndex, newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.multiPoint(points);
  }

  public static Polygon insertVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex, final Coordinates newPoint) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final ListCoordinatesList points = new ListCoordinatesList(
      rings.get(ringIndex));

    points.add(pointIndex, newPoint);

    rings.set(ringIndex, points);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.polygon(rings);
  }

  public static boolean isFromPoint(final Geometry geometry,
    final int[] vertexId) {
    final CoordinatesList points = getPoints(geometry, vertexId);
    if (points != null && points.size() > 0 && vertexId.length > 0) {
      final int index = vertexId[vertexId.length - 1];
      return index == 0;
    }
    return false;
  }

  public static boolean isToPoint(final Geometry geometry, final int[] vertexId) {
    final CoordinatesList points = getPoints(geometry, vertexId);
    if (points != null && points.size() > 0 && vertexId.length > 0) {
      final int index = vertexId[vertexId.length - 1];
      return index == points.size() - 1;
    }
    return false;
  }

  protected static CoordinatesList moveVertex(
    final CoordinatesList coordinatesList, final int pointIndex,
    final Coordinates newPoint) {
    final CoordinatesList points = new DoubleCoordinatesList(coordinatesList);
    setVertex(points, pointIndex, newPoint);
    return points;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T moveVertex(final Geometry geometry,
    final Coordinates newPoint, final int[] vertexId) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
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

  public static LineString moveVertex(final LineString line,
    final int pointIndex, final Coordinates newPoint) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final CoordinatesList points = moveVertex(coordinatesList, pointIndex,
      newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.lineString(points);
  }

  public static MultiPoint moveVertex(final MultiPoint multiPoint,
    final int pointIndex, final Coordinates newPoint) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final CoordinatesList points = moveVertex(coordinatesList, pointIndex,
      newPoint);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.multiPoint(points);
  }

  public static Polygon moveVertex(final Polygon polygon, final int ringIndex,
    final int pointIndex, final Coordinates newPoint) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList points = rings.get(ringIndex).clone();
    setVertex(points, pointIndex, newPoint);
    if (pointIndex == 0) {
      setVertex(points, points.size() - 1, newPoint);
    } else if (pointIndex == points.size() - 1) {
      setVertex(points, 0, newPoint);
    }
    rings.set(ringIndex, points);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.polygon(rings);
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G moveVertexIfEqual(final G geometry,
    final Coordinates originalLocation, final Coordinates newLocation,
    final int... vertexId) {
    final Coordinates coordinates = getVertex(geometry, vertexId);
    CoordinatesUtil.setElevation(newLocation, originalLocation);
    if (coordinates.equals2d(originalLocation)) {
      final Coordinates newCoordinates = CoordinatesUtil.setElevation(
        newLocation, originalLocation);
      return (G)moveVertex(geometry, newCoordinates, vertexId);
    } else {
      return geometry;
    }

  }

  public static void setVertex(final CoordinatesList points,
    final int pointIndex, final Coordinates point) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      points.setPoint(pointIndex, point);
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
      if (LineStringUtil.isCCW(line)) {
        return geometry;
      } else {
        return (G)LineStringUtil.reverse(line);
      }
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      boolean changed = false;
      final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
      for (int i = 0; i < pointsList.size(); i++) {
        CoordinatesList points = pointsList.get(i);
        final boolean counterClockwise = CoordinatesListUtil.isCCW(points);
        boolean pointsChanged;
        if (i == 0) {
          pointsChanged = !counterClockwise;
        } else {
          pointsChanged = counterClockwise;
        }
        if (pointsChanged) {
          changed = true;
          points = points.reverse();
          pointsList.set(i, points);
        }
      }
      if (changed) {
        return (G)GeometryFactory.getFactory(geometry).polygon(pointsList);
      } else {
        return geometry;
      }
    } else {
      return geometry;
    }
  }
}
