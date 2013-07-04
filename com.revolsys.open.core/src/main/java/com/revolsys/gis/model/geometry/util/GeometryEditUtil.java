package com.revolsys.gis.model.geometry.util;

import java.awt.Toolkit;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryEditUtil {

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

  protected static ListCoordinatesList appendVertex(
    final CoordinatesList coordinatesList, final Coordinates newVertex) {
    final ListCoordinatesList points = new ListCoordinatesList(coordinatesList);
    points.add(newVertex);
    return points;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T appendVertex(final Geometry geometry,
    final Coordinates newVertex, final int[] geometryId) {
    if (geometry != null && newVertex != null) {
      if (geometry instanceof Point) {
        return (T)geometry;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        return (T)appendVertex(line, newVertex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = geometryId[0];
        return (T)appendVertex(polygon, ringIndex, newVertex);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        return (T)appendVertex(multiPoint, newVertex);
      } else {
        final int partIndex = geometryId[0];
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final List<Geometry> parts = JtsGeometryUtil.getGeometries(geometry);
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            return (T)geometry;
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            newPart = appendVertex(line, newVertex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = geometryId[1];
            newPart = appendVertex(polygon, ringIndex, newVertex);
          }
          parts.set(partIndex, newPart);
        }
      }
    }
    return (T)geometry;
  }

  public static <T extends Geometry> T appendVertex(final Geometry geometry,
    final Point newVertex, final int[] partId) {
    return appendVertex(geometry, CoordinatesUtil.get(newVertex), partId);
  }

  public static LineString appendVertex(final LineString line,
    final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final ListCoordinatesList points = appendVertex(coordinatesList, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createLineString(points);
  }

  public static MultiPoint appendVertex(final MultiPoint multiPoint,
    final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final ListCoordinatesList points = appendVertex(coordinatesList, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.createMultiPoint(points);
  }

  public static Polygon appendVertex(final Polygon polygon,
    final int ringIndex, final Coordinates newVertex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList coordinatesList = rings.get(ringIndex);
    final ListCoordinatesList points = insertVertex(coordinatesList,
      coordinatesList.size() - 1, newVertex);

    rings.set(ringIndex, points);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.createPolygon(rings);
  }

  public static QuadTree<IndexedLineSegment> createLineSegmentQuadTree(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<QuadTree<IndexedLineSegment>> reference = JtsGeometryUtil.getGeometryProperty(
        geometry, "LineSegmentQuadTree");
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
          for (int partIndex = 0; partIndex < geometry.getNumGeometries(); partIndex++) {
            final Geometry part = geometry.getGeometryN(partIndex);
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
        JtsGeometryUtil.setGeometryProperty(geometry, "LineSegmentQuadTree",
          new SoftReference<QuadTree<IndexedLineSegment>>(index));
      }
      return index;
    }
    return new QuadTree<IndexedLineSegment>();
  }

  public static PointQuadTree<int[]> createPointQuadTree(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return new PointQuadTree<int[]>();
    } else {
      final Reference<PointQuadTree<int[]>> reference = JtsGeometryUtil.getGeometryProperty(
        geometry, "PointQuadTree");
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
          final Coordinates coordinates = CoordinatesUtil.get(point);
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
          for (int partIndex = 0; partIndex < geometry.getNumGeometries(); partIndex++) {
            final Geometry part = geometry.getGeometryN(partIndex);
            if (part instanceof Point) {
              final Point point = (Point)part;
              final Coordinates coordinates = CoordinatesUtil.get(point);
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
        JtsGeometryUtil.setGeometryProperty(geometry, "PointQuadTree",
          new SoftReference<PointQuadTree<int[]>>(index));
      }
      return index;
    }
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
        Toolkit.getDefaultToolkit().beep();
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
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final List<Geometry> parts = JtsGeometryUtil.getGeometries(geometry);
          final Geometry part = parts.get(partIndex);
          if (part instanceof Point) {
            parts.remove(partIndex);
            return geometryFactory.createGeometry(parts);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final Geometry newLine = deleteVertex(line, pointIndex);
            if (line != newLine) {
              parts.set(partIndex, newLine);
              return geometryFactory.createGeometry(parts);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int ringIndex = vertexId[1];
            final CoordinatesList points = rings.get(ringIndex);
            final CoordinatesList newPoints = deleteVertex(points, pointIndex);
            if (newPoints != points) {
              rings.set(ringIndex, newPoints);
              final Polygon newPart = geometryFactory.createPolygon(rings);
              parts.set(partIndex, newPart);
              return geometryFactory.createGeometry(parts);
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
        Toolkit.getDefaultToolkit().beep();
        return line;
      } else {
        return geometryFactory.createLineString(newPoints);
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
      Toolkit.getDefaultToolkit().beep();
      return polygon;
    } else {
      rings.set(ringIndex, newPoints);
      return geometryFactory.createPolygon(rings);
    }
  }

  public static Coordinates getCoordinatesOffset(final Geometry geometry,
    final int[] vertexId, final int offset) {
    final int[] newVertexId = vertexId.clone();
    newVertexId[vertexId.length - 1] = newVertexId[vertexId.length - 1]
      + offset;
    return getVertex(geometry, newVertexId);
  }

  public static Map<int[], Coordinates> getIndexOfVertices(
    final Geometry geometry) {
    final Map<int[], Coordinates> pointIndexes = new LinkedHashMap<int[], Coordinates>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        final Coordinates coordinates = CoordinatesUtil.get(point);
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
        for (int partIndex = 0; partIndex < geometry.getNumGeometries(); partIndex++) {
          final Geometry part = geometry.getGeometryN(partIndex);
          if (part instanceof Point) {
            final Point point = (Point)part;
            final Coordinates coordinates = CoordinatesUtil.get(point);
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
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final Geometry part = geometry.getGeometryN(partIndex);
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
    if (geometry == null || vertexId.length == 0) {
      return null;
    } else {
      int pointIndex = vertexId[vertexId.length - 1];
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        final Coordinates coordinates = CoordinatesUtil.get(point);
        return coordinates;
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
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final Geometry part = geometry.getGeometryN(partIndex);
          if (part instanceof Point) {
            final Point point = (Point)part;
            final Coordinates coordinates = CoordinatesUtil.get(point);
            return coordinates;
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

  protected static ListCoordinatesList insertVertex(
    final CoordinatesList coordinatesList, final int pointIndex,
    final Coordinates newVertex) {
    final ListCoordinatesList points = new ListCoordinatesList(coordinatesList);
    points.add(pointIndex, newVertex);
    return points;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Coordinates newVertex, final int[] vertexId) {
    if (geometry != null && newVertex != null) {
      if (geometry instanceof Point) {
        return (T)geometry;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final int pointIndex = vertexId[0];
        return (T)insertVertex(line, pointIndex, newVertex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        final int pointIndex = vertexId[1];
        return (T)insertVertex(polygon, ringIndex, pointIndex, newVertex);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        final int pointIndex = vertexId[0];
        return (T)insertVertex(multiPoint, pointIndex, newVertex);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final List<Geometry> parts = JtsGeometryUtil.getGeometries(geometry);
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            return (T)geometry;
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final int pointIndex = vertexId[1];
            newPart = insertVertex(line, pointIndex, newVertex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = vertexId[1];
            final int pointIndex = vertexId[2];
            newPart = insertVertex(polygon, ringIndex, pointIndex, newVertex);
          }
          parts.set(partIndex, newPart);
        }
      }
    }
    return (T)geometry;
  }

  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Point newVertex, final int[] vertexId) {
    return insertVertex(geometry, CoordinatesUtil.get(newVertex), vertexId);
  }

  public static LineString insertVertex(final LineString line,
    final int pointIndex, final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final ListCoordinatesList points = insertVertex(coordinatesList,
      pointIndex, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createLineString(points);
  }

  public static MultiPoint insertVertex(final MultiPoint multiPoint,
    final int pointIndex, final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final ListCoordinatesList points = insertVertex(coordinatesList,
      pointIndex, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.createMultiPoint(points);
  }

  public static Polygon insertVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex, final Coordinates newVertex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final ListCoordinatesList points = new ListCoordinatesList(
      rings.get(ringIndex));

    points.add(pointIndex, newVertex);

    rings.set(ringIndex, points);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.createPolygon(rings);
  }

  protected static CoordinatesList moveVertex(
    final CoordinatesList coordinatesList, final int pointIndex,
    final Coordinates newVertex) {
    final CoordinatesList points = coordinatesList.clone();
    setVertex(points, pointIndex, newVertex);
    return points;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T moveVertex(final Geometry geometry,
    final Coordinates newVertex, final int[] vertexId) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
        return (T)geometryFactory.createPoint(newVertex);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final int pointIndex = vertexId[0];
        return (T)moveVertex(line, pointIndex, newVertex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        final int pointIndex = vertexId[1];
        return (T)moveVertex(polygon, ringIndex, pointIndex, newVertex);
      } else if (geometry instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        final int pointIndex = vertexId[0];
        return (T)moveVertex(multiPoint, pointIndex, newVertex);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final List<Geometry> parts = JtsGeometryUtil.getGeometries(geometry);
          final Geometry part = parts.get(partIndex);
          Geometry newPart = part;
          if (part instanceof Point) {
            final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
            newPart = geometryFactory.createPoint(newVertex);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final int pointIndex = vertexId[1];
            newPart = moveVertex(line, pointIndex, newVertex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final int ringIndex = vertexId[1];
            final int pointIndex = vertexId[2];
            newPart = moveVertex(polygon, ringIndex, pointIndex, newVertex);
          }
          parts.set(partIndex, newPart);
        }
      }
    }
    return (T)geometry;
  }

  public static LineString moveVertex(final LineString line,
    final int pointIndex, final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final CoordinatesList points = moveVertex(coordinatesList, pointIndex,
      newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createLineString(points);
  }

  public static MultiPoint moveVertex(final MultiPoint multiPoint,
    final int pointIndex, final Coordinates newVertex) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(multiPoint);
    final CoordinatesList points = moveVertex(coordinatesList, pointIndex,
      newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(multiPoint);
    return geometryFactory.createMultiPoint(points);
  }

  public static Polygon moveVertex(final Polygon polygon, final int ringIndex,
    final int pointIndex, final Coordinates newVertex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList points = rings.get(ringIndex).clone();
    setVertex(points, pointIndex, newVertex);
    if (pointIndex == 0) {
      setVertex(points, points.size() - 1, newVertex);
    } else if (pointIndex == points.size() - 1) {
      setVertex(points, 0, newVertex);
    }
    rings.set(ringIndex, points);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
    return geometryFactory.createPolygon(rings);
  }

  public static void setVertex(final CoordinatesList points,
    final int pointIndex, final Coordinates point) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      points.setPoint(pointIndex, point);
    }
  }
}
