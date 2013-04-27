package com.revolsys.gis.model.geometry.util;

import java.util.List;

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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryEditUtil {

  public static QuadTree<IndexedLineSegment> createLineSegmentQuadTree(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      final QuadTree<IndexedLineSegment> index = new QuadTree<IndexedLineSegment>(
        geometryFactory);
      if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        int segmentIndex = 0;
        for (final LineSegment lineSegment : new CoordinatesListIndexLineSegmentIterator(
          line)) {
          final IndexedLineSegment indexedSegment = new IndexedLineSegment(
            geometryFactory, lineSegment, segmentIndex);
          index.insert(indexedSegment.getEnvelope(), indexedSegment);
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
            index.insert(indexedSegment.getEnvelope(), indexedSegment);
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
              index.insert(indexedSegment.getEnvelope(), indexedSegment);
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
                index.insert(indexedSegment.getEnvelope(), indexedSegment);
                segmentIndex++;
              }
            }
          }
        }
      }
      return index;
    }
    return new QuadTree<IndexedLineSegment>();
  }

  public static PointQuadTree<int[]> createPointQuadTree(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return new PointQuadTree<int[]>();
    } else {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      final PointQuadTree<int[]> index = new PointQuadTree<int[]>(
        geometryFactory);

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
      return index;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T deleteVertex(final Geometry geometry,
    final int[] vertexId) {
    if (geometry != null) {
      if (geometry instanceof Point) {
        return null;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final int pointIndex = vertexId[0];
        return (T)deleteVertex(line, pointIndex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final int ringIndex = vertexId[0];
        final int pointIndex = vertexId[1];
        return (T)deleteVertex(polygon, ringIndex, pointIndex);
      } else {
        final int partIndex = vertexId[0];
        if (partIndex >= 0 && partIndex < geometry.getNumGeometries()) {
          final List<Geometry> parts = JtsGeometryUtil.getGeometries(geometry);
          final Geometry part = parts.get(partIndex);
          if (part instanceof Point) {
            parts.remove(partIndex);
            final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
            return (T)geometryFactory.createGeometry(parts);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            final int pointIndex = vertexId[1];
            final LineString newLine = deleteVertex(line, pointIndex);
            if (line != newLine) {
              final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
              parts.set(partIndex, newLine);
              return (T)geometryFactory.createGeometry(parts);
            }
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int ringIndex = vertexId[1];
            final CoordinatesList points = rings.get(ringIndex);
            final int pointIndex = vertexId[2];
            final CoordinatesList newPoints = deleteVertex(3, points,
              pointIndex);
            if (newPoints != points) {
              rings.set(ringIndex, newPoints);
              final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
              final Polygon newPart = geometryFactory.createPolygon(rings);
              parts.set(partIndex, newPart);
              return (T)geometryFactory.createGeometry(parts);
            }
          }

        }
      }
    }
    return (T)geometry;
  }

  public static CoordinatesList deleteVertex(final int minPoints,
    final CoordinatesList points, final int pointIndex) {
    if (points.size() > minPoints) {
      if (pointIndex >= 0 && pointIndex < points.size()) {
        final ListCoordinatesList newPoints = new ListCoordinatesList(points);
        newPoints.remove(pointIndex);
        return newPoints;
      }
    }
    return points;
  }

  public static LineString deleteVertex(final LineString line,
    final int pointIndex) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesList newPoints = deleteVertex(2, points, pointIndex);
    if (newPoints != points) {
      return geometryFactory.createLineString(newPoints);
    } else {
      return line;
    }
  }

  public static Polygon deleteVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final CoordinatesList points = rings.get(ringIndex).clone();
    CoordinatesList newPoints;
    if (pointIndex == 0 || pointIndex == points.size() - 1) {
      newPoints = points.subList(1, points.size() - 1);
      final Coordinates firstPoint = newPoints.get(0);
      newPoints.setPoint(newPoints.size() - 1, firstPoint);
    } else {
      newPoints = deleteVertex(4, points, pointIndex);
    }
    if (newPoints == points || points.size() < 4) {
      return polygon;
    } else {
      rings.set(ringIndex, newPoints);
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(polygon);
      return geometryFactory.createPolygon(rings);
    }
  }

  public static Coordinates getCoordinates(final Geometry geometry,
    final int[] vertexId) {
    if (geometry == null) {
      return null;
    } else {
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        final Coordinates coordinates = CoordinatesUtil.get(point);
        return coordinates;
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final CoordinatesList points = CoordinatesListUtil.get(line);
        final int pointIndex = vertexId[0];
        return getVertex(points, pointIndex);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        final int ringIndex = vertexId[0];

        final CoordinatesList points = rings.get(ringIndex);
        int pointIndex = vertexId[1];
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
            final int pointIndex = vertexId[1];
            return getVertex(points, pointIndex);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
            final int vertexIndex = vertexId[1];
            final CoordinatesList points = rings.get(vertexIndex);
            final int pointIndex = vertexId[2];
            return getVertex(points, pointIndex);
          }
        }
      }
      return null;
    }
  }

  public static Coordinates getCoordinatesOffset(final Geometry geometry,
    final int[] vertexId, final int offset) {
    final int[] newVertexId = vertexId.clone();
    newVertexId[vertexId.length - 1] = newVertexId[vertexId.length - 1]
      + offset;
    return getCoordinates(geometry, newVertexId);
  }

  public static Coordinates getVertex(final CoordinatesList points,
    final int pointIndex) {
    if (pointIndex >= 0 && pointIndex < points.size()) {
      return points.get(pointIndex).cloneCoordinates();
    } else {
      return null;
    }
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

  @SuppressWarnings("unchecked")
  public static <T extends Geometry> T insertVertex(final Geometry geometry,
    final Coordinates newVertex, final int[] vertexId) {
    if (geometry != null) {
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

  public static LineString moveVertex(final LineString line,
    final int pointIndex, final Coordinates newVertex) {
    final CoordinatesList points = CoordinatesListUtil.get(line).clone();
    setVertex(points, pointIndex, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createLineString(points);
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

  public static LineString insertVertex(final LineString line,
    final int pointIndex, final Coordinates newVertex) {
    final ListCoordinatesList points = new ListCoordinatesList(
      CoordinatesListUtil.get(line));
    points.add(pointIndex + 1, newVertex);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createLineString(points);
  }

  public static Polygon insertVertex(final Polygon polygon,
    final int ringIndex, final int pointIndex, final Coordinates newVertex) {
    final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
    final ListCoordinatesList points = new ListCoordinatesList(
      rings.get(ringIndex));

    points.add(pointIndex + 1, newVertex);

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
