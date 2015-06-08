package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.algorithm.index.LineSegmentIndex;
import com.revolsys.gis.algorithm.linematch.LineMatchGraph;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponent;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.linemerge.LineMerger;

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

  public static double distance(final double aX1, final double aY1, final double aX2,
    final double aY2, final double bX1, final double bY1, final double bX2, final double bY2) {
    if (aX1 == aX2 && aY1 == aY2) {
      // Segment 1 is a zero length do point line distance
      return LineSegmentUtil.distanceLinePoint(bX1, bY1, bX2, bY2, aX1, aY1);
    } else if (bX1 == bX2 && aY1 == bY2) {
      // Segment 2 is a zero length do point line distance
      return LineSegmentUtil.distanceLinePoint(aX1, aY1, aX2, aY2, bX1, bY1);
    } else {

      // AB and CD are line segments
      /*
       * from comp.graphics.algo Solving the above for r and s yields
       * (Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy) r = ----------------------------- (eqn 1)
       * (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) s =
       * ----------------------------- (eqn 2) (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) Let
       * P be the position vector of the intersection point, then P=A+r(B-A) or
       * Px=Ax+r(Bx-Ax) Py=Ay+r(By-Ay) By examining the values of r & s, you can
       * also determine some other limiting conditions: If 0<=r<=1 & 0<=s<=1,
       * intersection exists r<0 or r>1 or s<0 or s>1 line segments do not
       * intersect If the denominator in eqn 1 is zero, AB & CD are parallel If
       * the numerator in eqn 1 is also zero, AB & CD are collinear.
       */
      final double rTop = (aY1 - bY1) * (bX2 - bX1) - (aX1 - bX1) * (bY2 - bY1);
      final double rBottom = (aX2 - aX1) * (bY2 - bY1) - (aY2 - aY1) * (bX2 - bX1);

      final double sTop = (aY1 - bY1) * (aX2 - aX1) - (aX1 - bX1) * (aY2 - aY1);
      final double sBottom = (aX2 - aX1) * (bY2 - bY1) - (aY2 - aY1) * (bX2 - bX1);

      if (rBottom == 0 || sBottom == 0) {
        return Math.min(LineSegmentUtil.distanceLinePoint(bX1, bY1, bX2, bY2, aX1, aY1), Math.min(
          LineSegmentUtil.distanceLinePoint(bX1, bY1, bX2, bY2, aX2, aY2),
          Math.min(LineSegmentUtil.distanceLinePoint(aX1, aY1, aX2, aY2, bX1, bY1),
            LineSegmentUtil.distanceLinePoint(aX1, aY1, aX2, aY2, bX2, bY2))));

      } else {
        final double s = sTop / sBottom;
        final double r = rTop / rBottom;

        if (r < 0 || r > 1 || s < 0 || s > 1) {
          // no intersection
          return Math.min(LineSegmentUtil.distanceLinePoint(bX1, bY1, bX2, bY2, aX1, aY1),
            Math.min(LineSegmentUtil.distanceLinePoint(bX1, bY1, bX2, bY2, aX2, aY2), Math.min(
              LineSegmentUtil.distanceLinePoint(aX1, aY1, aX2, aY2, bX1, bY1),
              LineSegmentUtil.distanceLinePoint(aX1, aY1, aX2, aY2, bX2, bY2))));
        }
        return 0.0;
      }
    }
  }

  public static double distance(final double x, final double y, final Geometry geometry,
    final double tolerance) {
    double distance = Double.MAX_VALUE;
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry part = geometry.getGeometry(i);
      if (part instanceof LineString) {
        final LineString line = (LineString)part;
        distance = Math.min(distance, distance(x, y, line, tolerance));
        if (distance <= tolerance) {
          return tolerance;
        }
      }
    }
    return distance;
  }

  public static double distance(final double x, final double y, final LineString line,
    final double tolerance) {
    final LineString coordinates = line;
    double minDistance = Double.MAX_VALUE;
    double x1 = coordinates.getX(0);
    double y1 = coordinates.getY(0);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      final double x2 = coordinates.getX(i);
      final double y2 = coordinates.getY(i);
      final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
      if (distance < minDistance) {
        if (distance <= tolerance) {
          return distance;
        } else {
          minDistance = distance;
        }
      }
      x1 = x2;
      y1 = y2;
    }
    return minDistance;
  }

  public static double distance(final LineString line1, final LineString line2,
    final double terminateDistance) {
    final double envelopeDistance = line1.getBoundingBox().distance(line2.getBoundingBox());
    if (envelopeDistance > terminateDistance) {
      return Double.MAX_VALUE;
    } else {
      double minDistance = Double.MAX_VALUE;
      final LineString coordinates1 = line1;
      final LineString coordinates2 = line2;
      double previousX1 = coordinates1.getCoordinate(0, 0);
      double previousY1 = coordinates1.getCoordinate(0, 1);
      for (int i = 1; i < coordinates1.getVertexCount(); i++) {
        final double x1 = coordinates1.getCoordinate(i, 0);
        final double y1 = coordinates1.getCoordinate(i, 1);

        double previousX2 = coordinates2.getCoordinate(0, 0);
        double previousY2 = coordinates2.getCoordinate(0, 1);
        for (int j = 1; j < coordinates2.getVertexCount(); j++) {
          final double x2 = coordinates2.getCoordinate(j, 0);
          final double y2 = coordinates2.getCoordinate(j, 1);
          final double distance = distance(previousX1, previousY1, x1, y1, previousX2, previousY2,
            x2, y2);
          if (distance <= terminateDistance) {
            return distance;
          } else if (distance < minDistance) {
            minDistance = distance;

          }
          previousX2 = x2;
          previousY2 = y2;
        }
        previousX1 = x1;
        previousY1 = y1;
      }
      return minDistance;
    }
  }

  public static double distance(final Point point, final Geometry geometry, final double tolerance) {
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y, geometry, tolerance);
  }

  // public static LineString cleanShortSegments(final LineString line) {
  // final GeometryFactory factory = GeometryFactory.getFactory(line);
  // if (line.getLength() > 2) {
  // final LineString points = CoordinatesListUtil.get(line);
  // DoubleListCoordinatesList newPoints = null;
  // int numRemoved = 0;
  // for (int i = 1; i < points.size(); i++) {
  // final double length = points.distance(i - 1, points, i);
  // if (length < 2) {
  // if (newPoints == null) {
  // newPoints = new DoubleListCoordinatesList(points);
  // }
  // if (newPoints.size() > 2) {
  // if (i == 1) {
  // final Point p1 = points.get(0);
  // final Point p2 = points.get(1);
  // final Point p3 = points.get(2);
  // final double angle = CoordinatesUtil.angle(p1, p2, p3);
  // if (angle > Math.toRadians(170)) {
  // newPoints.remove(1);
  // numRemoved++;
  // }
  // } else if (i == points.size() - 1) {
  // final Point p1 = points.get(i - 2);
  // final Point p2 = points.get(i - 1);
  // final Point p3 = points.get(i);
  // final double angle = CoordinatesUtil.angle(p1, p2, p3);
  // if (angle > Math.toRadians(170)) {
  // newPoints.remove(points.size() - 2 - numRemoved);
  // numRemoved++;
  // }
  // } else {
  // final Point p1 = points.get(i - 2);
  // final Point p2 = points.get(i - 1);
  // final Point p3 = points.get(i);
  // final Point p4 = points.get(i + 1);
  // final double angle1 = CoordinatesUtil.angle(p1, p2, p3);
  // final double angle2 = CoordinatesUtil.angle(p2, p3, p4);
  // boolean removed = false;
  // if (angle1 > angle2) {
  // if (angle1 > Math.toRadians(170)) {
  // if (i - 1 - numRemoved == 0) {
  // newPoints.remove(i - numRemoved);
  // } else {
  // newPoints.remove(i - 1 - numRemoved);
  //
  // }
  // removed = true;
  // }
  // } else if (angle2 > Math.toRadians(170)) {
  // newPoints.remove(i - numRemoved);
  // removed = true;
  // }
  // if (!removed) {
  // final Point midPoint = LineSegmentUtil.midPoint(factory,
  // p2, p3);
  // newPoints.setPoint(i - 1 - numRemoved, midPoint);
  // newPoints.remove(i - numRemoved);
  // }
  // numRemoved++;
  // }
  //
  // }
  // }
  // }
  // if (newPoints == null) {
  // return line;
  // } else {
  // return factory.lineString(newPoints);
  // }
  // } else {
  // return line;
  // }
  // }

  public static double distance(final Point point, final LineString line) {
    return distance(point, line, 0.0);
  }

  public static double distance(final Point point, final LineString line, final double tolerance) {
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y, line, tolerance);
  }

  public static double distance(final Point point, final LineString line, final int index,
    final double maxDistance) {
    final Point point2 = line.getVertex(index);
    return point.distance(point2);
  }

  public static double distanceFromStart(final LineString line, final Point point) {
    double segmentDistance = 0;
    double closestDistance = Double.MAX_VALUE;
    double distanceAlong = 0;
    for (final Segment segment : line.segments()) {
      final double distance = segment.distance(point);
      if (distance < line.getGeometryFactory().getResolutionXy()) {
        return segmentDistance + segment.getPoint(0).distance(point);
      } else if (distance < closestDistance) {
        closestDistance = distance;
        final double projectionFactor = segment.projectionFactor(point);
        if (projectionFactor <= 0) {
          if (segment.getSegmentIndex() == 0) {
            distanceAlong = -distance;
          } else {
            distanceAlong = segmentDistance;
          }
        } else if (projectionFactor >= 1) {
          if (segment.isLineEnd()) {
            distanceAlong = segmentDistance + segment.getLength() + distance;
          } else {
            distanceAlong = segmentDistance + segment.getLength();
          }
        } else {
          distanceAlong = segmentDistance + segment.distanceAlong(point);
        }
      }
      segmentDistance += segment.getLength();
    }
    return distanceAlong;
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
      point = point.convert(geometryFactory, 2);
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
      return Collections.<GeometryComponent, Double> singletonMap(closestComponent, closestDistance);
    }
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(final LineString line,
    final Point point) {
    final LineString points = line;
    return CoordinatesListUtil.findClosestSegmentAndCoordinate(points, point);
  }

  public static Point getClosestCoordinateOnLineString(final GeometryFactory precisionModel,
    final LineString line, final Point point, final double tolerance) {
    final Map<String, Number> result = LineStringUtil.findClosestSegmentAndCoordinate(line, point);
    final int segmentIndex = result.get(SEGMENT_INDEX).intValue();
    if (segmentIndex != -1) {
      final LineString coordinates = line;
      final int coordinateIndex = result.get(COORDINATE_INDEX).intValue();
      final double coordinateDistance = result.get(COORDINATE_DISTANCE).doubleValue();
      final double segmentDistance = result.get(SEGMENT_DISTANCE).doubleValue();
      if (coordinateIndex == 0) {
        final Point c0 = coordinates.getPoint(0);
        if (coordinateDistance < tolerance) {
          return c0;
        } else if (segmentDistance == 0) {
          return point;
        } else {
          Point c1;
          int i = 1;
          do {
            c1 = coordinates.getPoint(i);
            i++;
          } while (c1.equals(c0));
          if (CoordinatesUtil.isAcute(c1, c0, point)) {
            return LineSegmentUtil.pointAlong(precisionModel, c0, c1, point);
          } else {
            return c0;
          }
        }
      } else if (coordinateIndex == line.getVertexCount() - 1) {
        final Point cn = coordinates.getPoint(coordinates.getVertexCount() - 1);
        if (coordinateDistance == 0) {
          return cn;
        } else if (segmentDistance == 0) {
          return point;
        } else {
          Point cn1;
          int i = line.getVertexCount() - 2;
          do {
            cn1 = coordinates.getPoint(i);
            i++;
          } while (cn1.equals(cn));
          if (CoordinatesUtil.isAcute(cn1, cn, point)) {
            return LineSegmentUtil.pointAlong(precisionModel, cn1, cn, point);
          } else {
            return cn;
          }
        }
      } else {
        final Point cn1 = coordinates.getPoint(coordinateIndex - 1);
        final double cn1Distance = point.distance(cn1);
        final Point cn2 = coordinates.getPoint(coordinateIndex);
        final double cn2Distance = point.distance(cn2);
        if (cn1Distance < cn2Distance) {
          if (cn1Distance < tolerance) {
            return cn1;
          }
        } else if (cn2Distance < tolerance) {
          return cn2;
        }
        return LineSegmentUtil.pointAlong(precisionModel, cn1, cn2, point);
      }
    }
    return null;
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
              if (!((intersection.equals(2, firstCoord1) || intersection.equals(2, lastCoord1)) && (intersection.equals(
                2, firstCoord2) || intersection.equals(2, lastCoord2)))) {
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

  public static Point getEndPoint(final LineString line, final boolean fromPoint) {
    if (fromPoint) {
      return line.getPoint(0);
    } else {
      return line.getPoint(-1);
    }
  }

  @SuppressWarnings("unchecked")
  public static Collection<LineString> getMergedLines(final MultiLineString multiLineString) {
    final LineMerger merger = new LineMerger();
    merger.add(multiLineString);
    final Collection<LineString> lineStrings = merger.getMergedLineStrings();
    return lineStrings;
  }

  public static LineString getMergeLine(final MultiLineString multiLineString) {
    final Collection<LineString> lineStrings = getMergedLines(multiLineString);
    final int numLines = lineStrings.size();
    if (numLines == 1) {
      return lineStrings.iterator().next();
    } else {
      return null;
    }
  }

  public static boolean hasEndPoint(final LineString line, final Point point) {
    if (line.equalsVertex(2, 0, point)) {
      return true;
    } else {
      if (line.equalsVertex(2, -1, point)) {
        return true;
      } else {
        return false;
      }
    }
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
      final LineMatchGraph<LineString> graph = new LineMatchGraph<LineString>(line2);
      for (final LineString line : new LineStringCoordinatesListIterator(line1)) {
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

  public static LineString merge(final List<LineString> lines) {
    final Iterator<LineString> iterator = lines.iterator();
    if (!iterator.hasNext()) {
      return null;
    } else {
      LineString line = iterator.next();
      while (iterator.hasNext()) {
        final LineString nextLine = iterator.next();
        line = line.merge(nextLine);
      }
      return line;
    }
  }

  public static Point midPoint(final LineString line) {
    if (line.isEmpty()) {
      return null;
    } else {
      final int numPoints = line.getVertexCount();
      if (numPoints > 1) {
        final double totalLength = line.getLength();
        final LineString points = line;
        final double midPointLength = totalLength / 2;
        double currentLength = 0;
        for (int i = 1; i < numPoints && currentLength < midPointLength; i++) {
          final Point p1 = points.getPoint(i - 1);
          final Point p2 = points.getPoint(i);
          final double segmentLength = p1.distance(p2);
          if (segmentLength + currentLength >= midPointLength) {
            final GeometryFactory geometryFactory = line.getGeometryFactory();
            final Point midPoint = LineSegmentUtil.project(geometryFactory, p1, p2,
              (midPointLength - currentLength) / segmentLength);
            return geometryFactory.point(midPoint);

          }
          currentLength += segmentLength;
        }
        return null;
      } else {
        return line.getPoint(0);
      }
    }
  }

  public static List<LineString> split(final GeometryFactory geometryFactory,
    final LineString line, final LineSegmentIndex index, final double tolerance) {
    final LineString points = line;
    final Point firstCoordinate = points.getPoint(0);
    final int lastIndex = points.getVertexCount() - 1;
    final Point lastCoordinate = points.getPoint(lastIndex);
    int startIndex = 0;
    final List<LineString> newLines = new ArrayList<LineString>();
    Point startCoordinate = null;
    Point c0 = points.getPoint(0);
    for (int i = 1; i < points.getVertexCount(); i++) {
      final Point c1 = points.getPoint(i);

      final List<Geometry> intersectionPoints = index.queryIntersections(c0, c1);
      final List<Point> intersections = new ArrayList<Point>();
      for (final Geometry intersection : intersectionPoints) {
        for (final Point point : intersection.vertices()) {
          intersections.add(point.clonePoint());
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
      addLineString(geometryFactory, points, startCoordinate, startIndex, lastIndex, null, newLines);
    }
    return newLines;
  }

  public static List<LineString> split(final LineString line, final int segmentIndex,
    final Point point) {
    final List<LineString> lines = new ArrayList<LineString>();
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
