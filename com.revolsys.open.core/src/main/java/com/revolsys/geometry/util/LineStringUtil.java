package com.revolsys.geometry.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.algorithm.index.LineSegmentIndex;
import com.revolsys.geometry.algorithm.linematch.LineMatchGraph;
import com.revolsys.geometry.model.End;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;

public final class LineStringUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static LineString addElevation(final LineString original, final LineString update) {
    final int axisCount = update.getAxisCount();
    if (axisCount > 2) {
      final double[] coordinates = update.getCoordinates();

      final Point c0 = update.getPoint(0);
      if (Double.isNaN(update.getZ(0))) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[2] = z;
      }
      final Point cN = update.getPoint(update.getVertexCount() - 1);
      if (Double.isNaN(cN.getZ())) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[update.getVertexCount() * axisCount + 2] = z;
      }
      return update.getGeometryFactory().lineString(axisCount, coordinates);
    } else {
      return update;
    }
  }

  public static void addLineString(final GeometryFactory geometryFactory, final LineString points,
    final Point startPoint, final int startIndex, final int endIndex, final Point endPoint,
    final List<LineString> lines) {
    final int length = endIndex - startIndex + 1;
    final LineString newPoints = points.subLine(startPoint, startIndex, length, endPoint);
    if (newPoints.getVertexCount() > 1) {
      final LineString newLine = geometryFactory.lineString(newPoints);
      if (newLine.getLength() > 0) {
        lines.add(newLine);
      }
    }
  }

  /**
   * Compare the coordinates of the two lines up to the given dimension to see
   * if they have the same ordinate values in either the forward or reverse
   * direction.
   *
   * @param line1 The first line.
   * @param line2 The second line.
   * @param dimension The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection(final LineString line1, final LineString line2,
    final int dimension) {
    if (line1 == line2) {
      return true;
    } else {
      if (line1.equals(dimension, line2)) {
        return true;
      } else {
        return line1.reverse().equals(dimension, line2);
      }
    }
  }

  /**
   * Compare the 2D coordinates of the two lines to see if they have the same
   * ordinate values in either the forward or reverse direction.
   *
   * @param line1 The first line.
   * @param line2 The second line.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection2d(final LineString line1, final LineString line2) {
    return equalsIgnoreDirection(line1, line2, 2);
  }

  public static Map<GeometryComponent, Double> findClosestGeometryComponent(final LineString line,
    Point point) {
    if (line.isEmpty() || point.isEmpty()) {
      return Collections.emptyMap();
    } else {
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      GeometryComponent closestComponent = null;
      double closestDistance = Double.MAX_VALUE;
      for (final Segment segment : line.segments()) {
        if (segment.getSegmentIndex() == 0) {
          final Vertex from = segment.getGeometryVertex(0);
          if (from.equals(2, point)) {
            return Collections.<GeometryComponent, Double> singletonMap(from, 0.0);
          } else {
            closestDistance = from.distance(point);
            closestComponent = from;
          }
        }
        final Vertex to = segment.getGeometryVertex(1);
        if (to.equals(2, point)) {
          return Collections.<GeometryComponent, Double> singletonMap(to, 0.0);
        } else {
          final double toDistance = geometryFactory.makePrecise(0, to.distance(point));
          if (toDistance <= closestDistance) {
            if (!(closestComponent instanceof Vertex) || toDistance < closestDistance) {
              closestComponent = to;
              closestDistance = toDistance;
            }
          }
          final double segmentDistance = geometryFactory.makePrecise(0, segment.distance(point));
          if (segmentDistance == 0) {
            return Collections.<GeometryComponent, Double> singletonMap(segment, 0.0);
          } else if (segmentDistance < closestDistance) {
            closestComponent = segment;
            closestDistance = segmentDistance;
          }
        }
      }
      return Collections.<GeometryComponent, Double> singletonMap(closestComponent,
        closestDistance);
    }
  }

  public static Point getClosestEndsCoordinates(final LineString line, final Point coordinates) {
    final Point fromCoordinates = line.getPoint(0);
    final Point toCoordinates = line.getPoint(-1);
    if (fromCoordinates.distance(coordinates) <= toCoordinates.distance(coordinates)) {
      return fromCoordinates;
    } else {
      return toCoordinates;
    }
  }

  /**
   * Get the coordinate where two lines cross, or null if they don't cross.
   *
   * @param line1 The first line.
   * @param line2 The second line
   * @return The coordinate or null if they don't cross
   */
  public static Point getCrossingIntersection(final LineString line1, final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();

    final LineString coordinates1 = line1;
    final LineString coordinates2 = line2;
    final int numCoordinates1 = coordinates1.getVertexCount();
    final int numCoordinates2 = coordinates2.getVertexCount();
    final Point firstCoord1 = coordinates1.getPoint(0);
    final Point firstCoord2 = coordinates2.getPoint(0);
    final Point lastCoord1 = coordinates1.getPoint(numCoordinates1 - 1);
    final Point lastCoord2 = coordinates2.getPoint(numCoordinates2 - 2);

    Point previousCoord1 = firstCoord1;
    for (int i1 = 1; i1 < numCoordinates1; i1++) {
      final Point currentCoord1 = coordinates1.getPoint(i1);
      Point previousCoord2 = firstCoord2;

      for (int i2 = 1; i2 < numCoordinates2; i2++) {
        final Point currentCoord2 = coordinates2.getPoint(i2);

        intersector.computeIntersection(previousCoord1, currentCoord1, previousCoord2,
          currentCoord2);
        final int numIntersections = intersector.getIntersectionNum();
        if (intersector.hasIntersection()) {
          if (intersector.isProper()) {
            final Point intersection = intersector.getIntersection(0);
            return intersection;
          } else if (numIntersections == 1) {
            final Point intersection = intersector.getIntersection(0);
            if (i1 == 1 || i2 == 1 || i1 == numCoordinates1 - 1 || i2 == numCoordinates2 - 1) {
              if (!((intersection.equals(2, firstCoord1) || intersection.equals(2, lastCoord1))
                && (intersection.equals(2, firstCoord2) || intersection.equals(2, lastCoord2)))) {
                return intersection;
              }
            } else {
              return intersection;
            }
          } else if (intersector.isInteriorIntersection()) {
            for (int i = 0; i < numIntersections; i++) {
              final Point intersection = intersector.getIntersection(i);
              if (!Arrays.asList(currentCoord1, previousCoord1, currentCoord2, previousCoord2)
                .contains(intersection)) {
                return intersection;
              }
            }
          }

        }

        previousCoord2 = currentCoord2;
      }
      previousCoord1 = currentCoord1;
    }
    return null;

  }

  public static double getElevation(final Point point, final Point point1, final Point point2) {
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    final double fraction;
    if (point1.equals(2, point2)) {
      fraction = 0.5;
    } else {
      fraction = point.distance(point1) / point1.distance(point2);
    }
    final double z = z1 + (z2 - z1) * fraction;
    return z;
  }

  public static boolean hasEqualExact2d(final List<LineString> lines, final LineString newLine) {
    for (final LineString line : lines) {
      if (line.equals(2, newLine)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLoop(final Collection<LineString> lines) {
    for (final LineString line : lines) {
      if (line.isClosed()) {
        return true;
      }
    }
    return false;

  }

  public static boolean intersects(final LineString line1, final LineString line2) {
    if (line1.getBoundingBox().intersects(line2.getBoundingBox())) {
      final LineMatchGraph<LineString> graph = new LineMatchGraph<>(line2);
      for (final LineString line : line1.segments()) {
        if (graph.add(line)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isEndsWithinDistance(final LineString line1, final LineString line2,
    final double maxDistance) {
    final Point fromPoint = line1.getPoint(0);
    if (isEndsWithinDistance(line2, fromPoint, maxDistance)) {
      return true;
    } else {
      final Point toPoint = line1.getPoint(-1);
      if (isEndsWithinDistance(line2, toPoint, maxDistance)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistance(final LineString line, final Point point,
    final double maxDistance) {
    final Point fromPoint = line.getPoint(0);
    if (fromPoint.distance(point) < maxDistance) {
      return true;
    } else {
      final Point toPoint = line.getPoint(-1);
      if (toPoint.distance(point) < maxDistance) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistanceOfEnds(final LineString line1, final LineString line2,
    final double maxDistance) {
    final Point fromPoint = line1.getPoint(0);
    if (isWithinDistanceOfEnds(fromPoint, line2, maxDistance)) {
      final Point toPoint = line1.getPoint(-1);
      return isWithinDistanceOfEnds(toPoint, line2, maxDistance);
    } else {
      return false;
    }
  }

  /**
   * Check to see if the point is on any of the segments of the line.
   *
   * @param line The line.
   * @param point The point.
   * @return True if the point is on the line, false otherwise.
   * @see LineSegmentUtil#isPointOnLine(GeometryFactory, Coordinates,
   *      Coordinates, Point)
   */
  public static boolean isPointOnLine(final LineString line, final Point point) {
    final GeometryFactory factory = line.getGeometryFactory();

    if (line.equals(2, 0, point)) {
      return true;
    }
    for (final Segment segment : line.segments()) {
      final Point lineEnd = segment.getPoint(1);
      if (point.equals(2, lineEnd)) {
        return true;
      } else {
        final Point lineStart = segment.getPoint(0);

        if (LineSegmentUtil.isPointOnLine(factory, lineStart, lineEnd, point)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check to see if the point is on any of the segments of the line.
   *
   * @param line The line.
   * @param point The point.
   * @param maxDistance The maximum distance the point can be from the line.
   * @return True if the point is on the line, false otherwise.
   * @see LineSegmentUtil#isPointOnLine(Coordinates, Coordinates, Coordinates,
   *      double)
   */
  public static boolean isPointOnLine(final LineString line, final Point point,
    final double maxDistance) {
    if (line.equals(2, 0, point)) {
      return true;
    }
    for (final Segment segment : line.segments()) {
      final Point lineEnd = segment.getPoint(1);
      if (point.equals(2, lineEnd)) {
        return true;
      } else {
        final Point lineStart = segment.getPoint(0);

        if (LineSegmentUtil.isPointOnLine(lineStart, lineEnd, point, maxDistance)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isWithinDistance(final Point point, final LineString line, final int index,
    final double maxDistance) {
    final Point point2 = line.getVertex(index);
    return point.distance(point2) < maxDistance;
  }

  public static boolean isWithinDistanceOfEnds(final Point point, final LineString line,
    final double maxDistance) {
    if (isWithinDistance(point, line, 0, maxDistance)) {
      return true;
    } else {
      return isWithinDistance(point, line, line.getVertexCount() - 1, maxDistance);
    }
  }

  public static Point pointOffset(final LineString line, final End lineEnd, final double xOffset,
    double yOffset) {
    if (line.getLength() == 0) {
      return line.getFromPoint();
    } else {
      Point point1;
      Point point2;
      if (End.isFrom(lineEnd)) {
        point1 = line.getPoint(0);
        int i = 1;
        do {
          point2 = line.getPoint(i);
          i++;
        } while (point1.equals(point2));
      } else {
        point1 = line.getPoint(-1);
        int i = -2;
        do {
          point2 = line.getPoint(i);
          i--;
        } while (point1.equals(point2));
        yOffset = -yOffset;
      }
      return Points.pointOffset(point1, point2, xOffset, yOffset);
    }
  }

  public static List<LineString> split(final GeometryFactory geometryFactory, final LineString line,
    final LineSegmentIndex index, final double tolerance) {
    final LineString points = line;
    final Point firstCoordinate = points.getPoint(0);
    final int lastIndex = points.getVertexCount() - 1;
    final Point lastCoordinate = points.getPoint(lastIndex);
    int startIndex = 0;
    final List<LineString> newLines = new ArrayList<>();
    Point startCoordinate = null;
    Point c0 = points.getPoint(0);
    for (int i = 1; i < points.getVertexCount(); i++) {
      final Point c1 = points.getPoint(i);

      final List<Geometry> intersectionPoints = index.queryIntersections(c0, c1);
      final List<Point> intersections = new ArrayList<>();
      for (final Geometry intersection : intersectionPoints) {
        for (final Point point : intersection.vertices()) {
          intersections.add(point.newPointDouble());
        }
      }
      if (intersections.size() > 0) {
        if (intersections.size() > 1) {
          Collections.sort(intersections, new CoordinatesDistanceComparator(c0));
        }
        for (final Point intersection : intersections) {
          if (!(index.isWithinDistance(c0) && index.isWithinDistance(c1))) {
            if (i == 1 && intersection.distance(firstCoordinate) < tolerance) {
            } else if (i == lastIndex && intersection.distance(lastCoordinate) < tolerance) {
            } else {
              final double d0 = intersection.distance(c0);
              final double d1 = intersection.distance(c1);
              if (d0 <= tolerance) {
                if (d1 > tolerance) {
                  addLineString(geometryFactory, points, startCoordinate, startIndex, i - 1, null,
                    newLines);
                  startIndex = i - 1;
                  startCoordinate = null;
                } else {
                  addLineString(geometryFactory, points, startCoordinate, startIndex, i - 1,
                    intersection, newLines);
                  startIndex = i + 1;
                  startCoordinate = intersection;
                  c0 = intersection;
                }
              } else if (d1 <= tolerance) {
                addLineString(geometryFactory, points, startCoordinate, startIndex, i, null,
                  newLines);
                startIndex = i;
                startCoordinate = null;
              } else {
                addLineString(geometryFactory, points, startCoordinate, startIndex, i - 1,
                  intersection, newLines);
                startIndex = i;
                startCoordinate = intersection;
                c0 = intersection;
              }
            }
          }
        }
      }
      c0 = c1;
    }
    if (newLines.isEmpty()) {
      newLines.add(line);
    } else {
      addLineString(geometryFactory, points, startCoordinate, startIndex, lastIndex, null,
        newLines);
    }
    return newLines;
  }

  public static List<LineString> split(final LineString line, final int segmentIndex,
    final Point point) {
    final List<LineString> lines = new ArrayList<>();
    final LineString points = line;
    final boolean containsPoint = point.equals(points.getPoint(segmentIndex));
    final int axisCount = points.getAxisCount();
    int coords1Size;
    int coords2Size = points.getVertexCount() - segmentIndex;
    if (containsPoint) {
      coords1Size = segmentIndex + 1;
      coords2Size = points.getVertexCount() - segmentIndex;
    } else {
      coords1Size = segmentIndex + 2;
      coords2Size = points.getVertexCount() - segmentIndex;
    }
    final double[] coordinates1 = new double[coords1Size * axisCount];
    for (int i = 0; i < segmentIndex + 1; i++) {
      CoordinatesListUtil.setCoordinates(coordinates1, axisCount, i, points.getPoint(i));
    }

    final double[] coordinates2 = new double[coords2Size * axisCount];
    if (containsPoint) {
      for (int i = 0; i < segmentIndex + 1; i++) {
        CoordinatesListUtil.setCoordinates(coordinates2, axisCount, i,
          points.getPoint(segmentIndex + i));
      }
    } else {
      CoordinatesListUtil.setCoordinates(coordinates2, axisCount, coords1Size - 1, point);
      CoordinatesListUtil.setCoordinates(coordinates2, axisCount, 0, point);
      if (axisCount > 2) {
        final Point previous = points.getPoint(segmentIndex);
        final Point next = points.getPoint(segmentIndex + 1);
        final double z = getElevation(point, previous, next);
        coordinates1[(coords1Size - 1) * axisCount + 2] = z;
        coordinates2[2] = z;
      }

      for (int i = 1; i < coords2Size; i++) {
        CoordinatesListUtil.setCoordinates(coordinates2, axisCount, i,
          points.getPoint(segmentIndex + i));
      }
    }

    final GeometryFactory geometryFactory = line.getGeometryFactory();

    if (coords1Size > 1) {
      final LineString line1 = geometryFactory.lineString(axisCount, coordinates1);
      if (line1.getLength() > 0) {
        lines.add(line1);
      }
    }

    if (coords2Size > 1) {
      final LineString line2 = geometryFactory.lineString(axisCount, coordinates2);
      if (line2.getLength() > 0) {
        lines.add(line2);
      }
    }
    return lines;
  }

}
