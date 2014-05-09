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
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.operation.linemerge.LineMerger;
import com.revolsys.util.CollectionUtil;

public final class LineStringUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static LineString addElevation(final LineString original,
    final LineString update) {
    final int axisCount = update.getAxisCount();
    if (axisCount > 2) {
      final double[] coordinates = update.getCoordinatesList().getCoordinates();

      final Coordinates c0 = update.getCoordinate(0);
      if (Double.isNaN(update.getZ(0))) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[2] = z;
      }
      final Coordinates cN = update.getCoordinate(update.getVertexCount() - 1);
      if (Double.isNaN(cN.getZ())) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[update.getVertexCount() * axisCount + 2] = z;
      }
      return update.getGeometryFactory().lineString(axisCount, coordinates);
    } else {
      return update;
    }
  }

  // public static LineString cleanShortSegments(final LineString line) {
  // final GeometryFactory factory = GeometryFactory.getFactory(line);
  // if (line.getLength() > 2) {
  // final CoordinatesList points = CoordinatesListUtil.get(line);
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
  // final Coordinates p1 = points.get(0);
  // final Coordinates p2 = points.get(1);
  // final Coordinates p3 = points.get(2);
  // final double angle = CoordinatesUtil.angle(p1, p2, p3);
  // if (angle > Math.toRadians(170)) {
  // newPoints.remove(1);
  // numRemoved++;
  // }
  // } else if (i == points.size() - 1) {
  // final Coordinates p1 = points.get(i - 2);
  // final Coordinates p2 = points.get(i - 1);
  // final Coordinates p3 = points.get(i);
  // final double angle = CoordinatesUtil.angle(p1, p2, p3);
  // if (angle > Math.toRadians(170)) {
  // newPoints.remove(points.size() - 2 - numRemoved);
  // numRemoved++;
  // }
  // } else {
  // final Coordinates p1 = points.get(i - 2);
  // final Coordinates p2 = points.get(i - 1);
  // final Coordinates p3 = points.get(i);
  // final Coordinates p4 = points.get(i + 1);
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
  // final Coordinates midPoint = LineSegmentUtil.midPoint(factory,
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

  public static void addLineString(final GeometryFactory geometryFactory,
    final CoordinatesList points, final Coordinates startPoint,
    final int startIndex, final int endIndex, final Coordinates endPoint,
    final List<LineString> lines) {
    final int length = endIndex - startIndex + 1;
    final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
      startPoint, startIndex, length, endPoint);
    if (newPoints.size() > 1) {
      final LineString newLine = geometryFactory.lineString(newPoints);
      if (newLine.getLength() > 0) {
        lines.add(newLine);
      }
    }
  }

  public static double distance(final Coordinates point,
    final Geometry geometry, final double tolerance) {
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y, geometry, tolerance);
  }

  public static double distance(final Coordinates point, final LineString line) {
    return distance(point, line, 0.0);
  }

  public static double distance(final Coordinates point, final LineString line,
    final double tolerance) {
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y, line, tolerance);
  }

  public static double distance(final Coordinates point, final LineString line,
    final int index, final double maxDistance) {
    final Coordinates point2 = CoordinatesListUtil.get(line, index);
    return point.distance(point2);
  }

  public static double distance(final double aX1, final double aY1,
    final double aX2, final double aY2, final double bX1, final double bY1,
    final double bX2, final double bY2) {
    if (aX1 == aX2 && aY1 == aY2) {
      // Segment 1 is a zero length do point line distance
      return LineSegmentUtil.distance(bX1, bY1, bX2, bY2, aX1, aY1);
    } else if (bX1 == bX2 && aY1 == bY2) {
      // Segment 2 is a zero length do point line distance
      return LineSegmentUtil.distance(aX1, aY1, aX2, aY2, bX1, bY1);
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
      final double rBottom = (aX2 - aX1) * (bY2 - bY1) - (aY2 - aY1)
        * (bX2 - bX1);

      final double sTop = (aY1 - bY1) * (aX2 - aX1) - (aX1 - bX1) * (aY2 - aY1);
      final double sBottom = (aX2 - aX1) * (bY2 - bY1) - (aY2 - aY1)
        * (bX2 - bX1);

      if (rBottom == 0 || sBottom == 0) {
        return Math.min(LineSegmentUtil.distance(bX1, bY1, bX2, bY2, aX1, aY1),
          Math.min(LineSegmentUtil.distance(bX1, bY1, bX2, bY2, aX2, aY2),
            Math.min(LineSegmentUtil.distance(aX1, aY1, aX2, aY2, bX1, bY1),
              LineSegmentUtil.distance(aX1, aY1, aX2, aY2, bX2, bY2))));

      } else {
        final double s = sTop / sBottom;
        final double r = rTop / rBottom;

        if ((r < 0) || (r > 1) || (s < 0) || (s > 1)) {
          // no intersection
          return Math.min(
            LineSegmentUtil.distance(bX1, bY1, bX2, bY2, aX1, aY1), Math.min(
              LineSegmentUtil.distance(bX1, bY1, bX2, bY2, aX2, aY2), Math.min(
                LineSegmentUtil.distance(aX1, aY1, aX2, aY2, bX1, bY1),
                LineSegmentUtil.distance(aX1, aY1, aX2, aY2, bX2, bY2))));
        }
        return 0.0;
      }
    }
  }

  public static double distance(final double x, final double y,
    final Geometry geometry, final double tolerance) {
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

  public static double distance(final double x, final double y,
    final LineString line, final double tolerance) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    double minDistance = Double.MAX_VALUE;
    double x1 = coordinates.getX(0);
    double y1 = coordinates.getY(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final double x2 = coordinates.getX(i);
      final double y2 = coordinates.getY(i);
      final double distance = LineSegmentUtil.distance(x1, y1, x2, y2, x, y);
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
    final double envelopeDistance = line1.getBoundingBox().distance(
      line2.getBoundingBox());
    if (envelopeDistance > terminateDistance) {
      return Double.MAX_VALUE;
    } else {
      double minDistance = Double.MAX_VALUE;
      final CoordinatesList coordinates1 = line1.getCoordinatesList();
      final CoordinatesList coordinates2 = line2.getCoordinatesList();
      double previousX1 = coordinates1.getValue(0, 0);
      double previousY1 = coordinates1.getValue(0, 1);
      for (int i = 1; i < coordinates1.size(); i++) {
        final double x1 = coordinates1.getValue(i, 0);
        final double y1 = coordinates1.getValue(i, 1);

        double previousX2 = coordinates2.getValue(0, 0);
        double previousY2 = coordinates2.getValue(0, 1);
        for (int j = 1; j < coordinates2.size(); j++) {
          final double x2 = coordinates2.getValue(j, 0);
          final double y2 = coordinates2.getValue(j, 1);
          final double distance = distance(previousX1, previousY1, x1, y1,
            previousX2, previousY2, x2, y2);
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

  /**
   * Compare the coordinates of the two lines up to the given dimension to see
   * if they have the same ordinate values.
   * 
   * @param line1 The first line.
   * @param line2 The second line.
   * @param axisCount The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsExact(final LineString line1,
    final LineString line2, final int axisCount) {
    if (line1 == line2) {
      return true;
    } else {
      final CoordinatesList coordinates1 = CoordinatesListUtil.get(line1);
      final CoordinatesList coordinates2 = CoordinatesListUtil.get(line2);
      return coordinates1.equals(coordinates2, axisCount);
    }
  }

  /**
   * Compare the 2D coordinates of the two lines to see if they have the same
   * ordinate values.
   * 
   * @param line1 The first line.
   * @param line2 The second line.
   * @return True if the coordinates match.
   */
  public static boolean equalsExact2d(final LineString line1,
    final LineString line2) {
    return equalsExact(line1, line2, 2);
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
  public static boolean equalsIgnoreDirection(final LineString line1,
    final LineString line2, final int dimension) {
    if (line1 == line2) {
      return true;
    } else {
      final CoordinatesList coordinates1 = CoordinatesListUtil.get(line1);
      final CoordinatesList coordinates2 = CoordinatesListUtil.get(line2);
      if (coordinates1.equals(coordinates2, dimension)) {
        return true;
      } else {
        return coordinates1.reverse().equals(coordinates2, dimension);
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
  public static boolean equalsIgnoreDirection2d(final LineString line1,
    final LineString line2) {
    return equalsIgnoreDirection(line1, line2, 2);
  }

  public static boolean equalWithinDistance(final LineString line1,
    final LineString line2, final double maxDistance) {
    final CoordinatesList points1 = CoordinatesListUtil.get(line1);
    final CoordinatesList points2 = CoordinatesListUtil.get(line2);
    return CoordinatesListUtil.equalWithinTolerance(points1, points2,
      maxDistance);
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(
    final LineString line, final Coordinates point) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    return CoordinatesListUtil.findClosestSegmentAndCoordinate(points, point);
  }

  public static List<LineString> getAll(final Geometry geometry) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
      final Geometry part = geometry.getGeometry(partIndex);
      if (part instanceof LineString) {
        final LineString line = (LineString)part;
        lines.add(line);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        lines.add(polygon.getExteriorRing());
        for (int ringIndex = 0; ringIndex < polygon.getNumInteriorRing(); ringIndex++) {
          final LineString ring = polygon.getInteriorRing(ringIndex);
          lines.add(ring);
        }
      }
    }
    return lines;
  }

  public static Coordinates getClosestCoordinateOnLineString(
    final GeometryFactory precisionModel, final LineString line,
    final Coordinates point, final double tolerance) {
    final Map<String, Number> result = LineStringUtil.findClosestSegmentAndCoordinate(
      line, point);
    final int segmentIndex = result.get(SEGMENT_INDEX).intValue();
    if (segmentIndex != -1) {
      final CoordinatesList coordinates = CoordinatesListUtil.get(line);
      final int coordinateIndex = result.get(COORDINATE_INDEX).intValue();
      final double coordinateDistance = result.get(COORDINATE_DISTANCE)
        .doubleValue();
      final double segmentDistance = result.get(SEGMENT_DISTANCE).doubleValue();
      if (coordinateIndex == 0) {
        final Coordinates c0 = coordinates.get(0);
        if (coordinateDistance < tolerance) {
          return c0;
        } else if (segmentDistance == 0) {
          return point;
        } else {
          Coordinates c1;
          int i = 1;
          do {
            c1 = coordinates.get(i);
            i++;
          } while (c1.equals(c0));
          if (CoordinatesUtil.isAcute(c1, c0, point)) {
            return LineSegmentUtil.pointAlong(precisionModel, c0, c1, point);
          } else {
            return c0;
          }
        }
      } else if (coordinateIndex == line.getVertexCount() - 1) {
        final Coordinates cn = coordinates.get(coordinates.size() - 1);
        if (coordinateDistance == 0) {
          return cn;
        } else if (segmentDistance == 0) {
          return point;
        } else {
          Coordinates cn1;
          int i = line.getVertexCount() - 2;
          do {
            cn1 = coordinates.get(i);
            i++;
          } while (cn1.equals(cn));
          if (CoordinatesUtil.isAcute(cn1, cn, point)) {
            return LineSegmentUtil.pointAlong(precisionModel, cn1, cn, point);
          } else {
            return cn;
          }
        }
      } else {
        final Coordinates cn1 = coordinates.get(coordinateIndex - 1);
        final double cn1Distance = point.distance(cn1);
        final Coordinates cn2 = coordinates.get(coordinateIndex);
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

  public static Coordinates getClosestEndsCoordinates(final LineString line,
    final Coordinates coordinates) {
    final Coordinates fromCoordinates = getFromCoordinates(line);
    final Coordinates toCoordinates = getToCoordinates(line);
    if (fromCoordinates.distance(coordinates) <= toCoordinates.distance(coordinates)) {
      return fromCoordinates;
    } else {
      return toCoordinates;
    }
  }

  public static Coordinates getCoordinates(final LineString line, final int i) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    return points.get(i);
  }

  /**
   * Get the coordinate where two lines cross, or null if they don't cross.
   * 
   * @param line1 The first line.
   * @param line2 The second line
   * @return The coordinate or null if they don't cross
   */
  public static Coordinates getCrossingIntersection(final LineString line1,
    final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();

    final CoordinatesList coordinates1 = CoordinatesListUtil.get(line1);
    final CoordinatesList coordinates2 = CoordinatesListUtil.get(line2);
    final int numCoordinates1 = coordinates1.size();
    final int numCoordinates2 = coordinates2.size();
    final Coordinates firstCoord1 = coordinates1.getCoordinate(0);
    final Coordinates firstCoord2 = coordinates2.getCoordinate(0);
    final Coordinates lastCoord1 = coordinates1.getCoordinate(numCoordinates1 - 1);
    final Coordinates lastCoord2 = coordinates2.getCoordinate(numCoordinates2 - 2);

    Coordinates previousCoord1 = firstCoord1;
    for (int i1 = 1; i1 < numCoordinates1; i1++) {
      final Coordinates currentCoord1 = coordinates1.getCoordinate(i1);
      Coordinates previousCoord2 = firstCoord2;

      for (int i2 = 1; i2 < numCoordinates2; i2++) {
        final Coordinates currentCoord2 = coordinates2.getCoordinate(i2);

        intersector.computeIntersection(previousCoord1, currentCoord1,
          previousCoord2, currentCoord2);
        final int numIntersections = intersector.getIntersectionNum();
        if (intersector.hasIntersection()) {
          if (intersector.isProper()) {
            final Coordinates intersection = intersector.getIntersection(0);
            return CoordinatesUtil.get(intersection);
          } else if (numIntersections == 1) {
            final Coordinates intersection = intersector.getIntersection(0);
            if (i1 == 1 || i2 == 1 || i1 == numCoordinates1 - 1
              || i2 == numCoordinates2 - 1) {
              if (!((intersection.equals2d(firstCoord1) || intersection.equals2d(lastCoord1)) && (intersection.equals2d(firstCoord2) || intersection.equals2d(lastCoord2)))) {
                return CoordinatesUtil.get(intersection);
              }
            } else {
              return CoordinatesUtil.get(intersection);
            }
          } else if (intersector.isInteriorIntersection()) {
            for (int i = 0; i < numIntersections; i++) {
              final Coordinates intersection = intersector.getIntersection(i);
              if (!Arrays.asList(currentCoord1, previousCoord1, currentCoord2,
                previousCoord2).contains(intersection)) {
                return CoordinatesUtil.get(intersection);
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

  public static double getElevation(final Coordinates point,
    final Coordinates point1, final Coordinates point2) {
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    final double fraction;
    if (point1.equals2d(point2)) {
      fraction = 0.5;
    } else {
      fraction = point.distance(point1) / point1.distance(point2);
    }
    final double z = z1 + (z2 - z1) * (fraction);
    return z;
  }

  public static Point getEndPoint(final LineString line, final boolean fromPoint) {
    if (fromPoint) {
      return getFromPoint(line);
    } else {
      return getToPoint(line);
    }
  }

  public static Coordinates getFromCoordinates(final LineString line) {
    return getCoordinates(line, 0);
  }

  public static Point getFromPoint(final LineString line) {
    return getPoint(line, 0);
  }

  public static double getLength(final MultiLineString lines) {
    double length = 0;
    for (int i = 0; i < lines.getGeometryCount(); i++) {
      final LineString line = (LineString)lines.getGeometry(i);
      length += line.getLength();
    }
    return length;
  }

  @SuppressWarnings("unchecked")
  public static Collection<LineString> getMergedLines(
    final MultiLineString multiLineString) {
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

  public static Point getPoint(final LineString line, final int index) {
    final Coordinates coordinates = getCoordinates(line, index);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.point(coordinates);
  }

  public static Coordinates getToCoordinates(final LineString line) {
    final int index = line.getVertexCount() - 1;
    return getCoordinates(line, index);
  }

  public static Point getToPoint(final LineString line) {
    final Coordinates coordinates = getToCoordinates(line);
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.point(coordinates);
  }

  public static boolean hasEndPoint(final LineString line, final Point point) {
    final Point fromPoint = getFromPoint(line);
    if (fromPoint.equals(point)) {
      return true;
    } else {
      final Point toPoint = getToPoint(line);
      if (toPoint.equals(point)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean hasEqualExact2d(final List<LineString> lines,
    final LineString newLine) {
    for (final LineString line : lines) {
      if (equalsExact2d(line, newLine)) {
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

  /**
   * Get the portion of the line segment between c1 and c2 which intersects the
   * line string geometry. If there is no intersection null will be returned,
   * otherwise a point, line, multi-point, multi-line, or multi-geometry
   * containing point and lines depending on what intersections occur.
   * 
   * @param lineStart The coordinates of the start of the segment.
   * @param lineEnd The coordinates of the end of the segment
   * @param line The line to intersect.
   * @return The geometry of the intersection.
   */
  public static Geometry intersection(final Coordinates lineStart,
    final Coordinates lineEnd, final LineString line) {
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final List<CoordinatesList> intersections = new ArrayList<CoordinatesList>();
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Iterator<Coordinates> iterator = points.iterator();
    Coordinates previousPoint = iterator.next();
    while (iterator.hasNext()) {
      final Coordinates nextPoint = iterator.next();
      final CoordinatesList intersectionPoints = LineSegmentUtil.getIntersection(
        factory, lineStart, lineEnd, previousPoint, nextPoint);
      if (intersectionPoints.size() > 0) {
        if (intersectionPoints.size() == 1 && !intersections.isEmpty()) {
          final Coordinates point = intersectionPoints.get(0);
          final CoordinatesList lastIntersection = intersections.get(intersections.size() - 1);
          if (!lastIntersection.contains(point)) {
            intersections.add(intersectionPoints);

          }
        } else if (intersectionPoints.size() == 2 && !intersections.isEmpty()) {
          final Coordinates start = intersectionPoints.get(0);
          final Coordinates end = intersectionPoints.get(1);
          CoordinatesList lastIntersection = intersections.get(intersections.size() - 1);
          if (lastIntersection.size() == 1) {
            final Coordinates point = lastIntersection.get(0);
            if (start.equals2d(point) || end.equals2d(point)) {
              intersections.set(intersections.size() - 1, intersectionPoints);
            } else {
              intersections.add(intersectionPoints);
            }
          } else {
            final Coordinates lastStart = lastIntersection.get(0);
            final Coordinates lastEnd = lastIntersection.get(lastIntersection.size() - 1);
            if (start.equals2d(lastEnd)) {
              lastIntersection = CoordinatesListUtil.subList(
                intersectionPoints, null, 0, lastIntersection.size(), end);
              intersections.set(intersections.size() - 1, lastIntersection);
            } else if (end.equals2d(lastStart)) {
              lastIntersection = CoordinatesListUtil.subList(
                intersectionPoints, start, 0, lastIntersection.size(), null);
              intersections.set(intersections.size() - 1, lastIntersection);
            } else {
              intersections.add(intersectionPoints);
            }
          }
        } else {
          intersections.add(intersectionPoints);
        }
      }
      previousPoint = nextPoint;
    }

    if (intersections.isEmpty()) {
      return null;
    } else if (intersections.size() == 1) {
      final CoordinatesList intersectionPoints = intersections.get(0);
      if (intersectionPoints.size() == 1) {
        final Coordinates point = intersectionPoints.get(0);
        return factory.point(point);
      } else {
        return factory.lineString(intersectionPoints);
      }
    } else {
      final Collection<Geometry> geometries = new ArrayList<Geometry>();
      for (final CoordinatesList intersection : intersections) {
        if (intersection.size() == 1) {
          final Coordinates point = intersection.get(0);
          geometries.add(factory.point(point));
        } else {
          geometries.add(factory.lineString(intersection));
        }
      }
      final Geometry geometry = factory.buildGeometry(geometries);
      return geometry.union();
    }
  }

  public static boolean intersects(final LineString line1,
    final LineString line2) {
    if (line1.getBoundingBox().intersects(line2.getBoundingBox())) {
      final LineMatchGraph<LineString> graph = new LineMatchGraph<LineString>(
        line2);
      for (final LineString line : new LineStringCoordinatesListIterator(line1)) {
        if (graph.add(line)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isEndsWithinDistance(final LineString line,
    final Coordinates point, final double maxDistance) {
    final Coordinates fromPoint = getFromCoordinates(line);
    if (fromPoint.distance(point) < maxDistance) {
      return true;
    } else {
      final Coordinates toPoint = getToCoordinates(line);
      if (toPoint.distance(point) < maxDistance) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistance(final LineString line1,
    final LineString line2, final double maxDistance) {
    final Coordinates fromPoint = getFromCoordinates(line1);
    if (isEndsWithinDistance(line2, fromPoint, maxDistance)) {
      return true;
    } else {
      final Coordinates toPoint = getToCoordinates(line1);
      if (isEndsWithinDistance(line2, toPoint, maxDistance)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistanceOfEnds(final LineString line1,
    final LineString line2, final double maxDistance) {
    final Coordinates fromPoint = getFromCoordinates(line1);
    if (isWithinDistanceOfEnds(fromPoint, line2, maxDistance)) {
      final Coordinates toPoint = getToCoordinates(line1);
      return isWithinDistanceOfEnds(toPoint, line2, maxDistance);
    } else {
      return false;
    }
  }

  public static boolean isFromPoint(final LineString line,
    final Coordinates point) {
    final Coordinates fromPoint = getFromCoordinates(line);
    return fromPoint.equals(point);
  }

  /**
   * Check to see if the point is on any of the segments of the line.
   * 
   * @param line The line.
   * @param point The point.
   * @return True if the point is on the line, false otherwise.
   * @see LineSegmentUtil#isPointOnLine(GeometryFactory, Coordinates,
   *      Coordinates, Coordinates)
   */
  public static boolean isPointOnLine(final LineString line,
    final Coordinates point) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final CoordinatesListCoordinates lineStart = new CoordinatesListCoordinates(
      points);
    if (point.equals2d(lineStart)) {
      return true;
    }
    final CoordinatesListCoordinates lineEnd = new CoordinatesListCoordinates(
      points);
    for (int i = 1; i < points.size(); i++) {
      lineStart.setIndex(i - 1);
      lineEnd.setIndex(i);
      if (point.equals2d(lineEnd)) {
        return true;
      }
      if (LineSegmentUtil.isPointOnLine(factory, lineStart, lineEnd, point)) {
        return true;
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
  public static boolean isPointOnLine(final LineString line,
    final Coordinates point, final double maxDistance) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesListCoordinates lineStart = new CoordinatesListCoordinates(
      points);
    if (point.equals2d(lineStart)) {
      return true;
    }
    final CoordinatesListCoordinates lineEnd = new CoordinatesListCoordinates(
      points);
    for (int i = 1; i < points.size(); i++) {
      lineStart.setIndex(i - 1);
      lineEnd.setIndex(i);
      if (point.equals2d(lineEnd)) {
        return true;
      }
      if (LineSegmentUtil.isPointOnLine(lineStart, lineEnd, point, maxDistance)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isToPoint(final LineString line, final Coordinates point) {
    final Coordinates toPoint = getToCoordinates(line);
    return toPoint.equals(point);
  }

  public static boolean isWithinDistance(final Coordinates point,
    final LineString line, final int index, final double maxDistance) {
    final Coordinates point2 = CoordinatesListUtil.get(line, index);
    return point.distance(point2) < maxDistance;
  }

  public static boolean isWithinDistanceOfEnds(final Coordinates point,
    final LineString line, final double maxDistance) {
    if (isWithinDistance(point, line, 0, maxDistance)) {
      return true;
    } else {
      return isWithinDistance(point, line, line.getVertexCount() - 1,
        maxDistance);
    }
  }

  public static LineString merge(final Coordinates point,
    final LineString line1, final LineString line2) {
    final CoordinatesList coordinates1 = CoordinatesListUtil.get(line1);
    final CoordinatesList coordinates2 = CoordinatesListUtil.get(line2);
    final CoordinatesList coordinates = CoordinatesListUtil.merge(point,
      coordinates1, coordinates2);
    final GeometryFactory factory = GeometryFactory.getFactory(line1);
    final LineString line = factory.lineString(coordinates);
    GeometryProperties.copyUserData(line1, line);
    return line;
  }

  /**
   * Merge two lines that share common coordinates at either the start or end.
   * If the lines touch only at their start coordinates, the line2 will be
   * reversed and joined before the start of line1. If the two lines touch only
   * at their end coordinates, the line2 will be reversed and joined after the
   * end of line1.
   * 
   * @param line1 The first line.
   * @param line2 The second line.
   * @return The new line string
   */
  public static LineString merge(final LineString line1, final LineString line2) {
    final CoordinatesList coordinates1 = CoordinatesListUtil.get(line1);
    final CoordinatesList coordinates2 = CoordinatesListUtil.get(line2);
    final CoordinatesList coordinates = CoordinatesListUtil.merge(coordinates1,
      coordinates2);
    final GeometryFactory factory = GeometryFactory.getFactory(line1);
    final LineString line = factory.lineString(coordinates);
    GeometryProperties.copyUserData(line1, line);

    return line;
  }

  public static Point midPoint(final LineString line) {
    if (line.isEmpty()) {
      return null;
    } else {
      final int numPoints = line.getVertexCount();
      if (numPoints > 1) {
        final double totalLength = line.getLength();
        final CoordinatesList points = CoordinatesListUtil.get(line);
        final double midPointLength = totalLength / 2;
        double currentLength = 0;
        for (int i = 1; i < numPoints && currentLength < midPointLength; i++) {
          final Coordinates p1 = points.get(i - 1);
          final Coordinates p2 = points.get(i);
          final double segmentLength = p1.distance(p2);
          if (segmentLength + currentLength >= midPointLength) {
            final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
            final Coordinates midPoint = LineSegmentUtil.project(
              geometryFactory, p1, p2, (midPointLength - currentLength)
                / segmentLength);
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

  public static LineString reverse(final LineString line) {
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    final CoordinatesList reverseCoordinates = coordinates.reverse();
    final LineString newLine = factory.lineString(reverseCoordinates);
    GeometryProperties.copyUserData(line, newLine);
    return newLine;
  }

  public static List<LineString> split(final GeometryFactory geometryFactory,
    final LineString line, final LineSegmentIndex index, final double tolerance) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Coordinates firstCoordinate = points.get(0);
    final int lastIndex = points.size() - 1;
    final Coordinates lastCoordinate = points.get(lastIndex);
    int startIndex = 0;
    final List<LineString> newLines = new ArrayList<LineString>();
    Coordinates startCoordinate = null;
    Coordinates c0 = points.get(0);
    for (int i = 1; i < points.size(); i++) {
      final Coordinates c1 = points.get(i);

      final List<CoordinatesList> intersectionPoints = index.queryIntersections(
        c0, c1);
      final List<Coordinates> intersections = new ArrayList<Coordinates>();
      for (final CoordinatesList coordinatesList : intersectionPoints) {
        intersections.addAll(coordinatesList.getList());
      }
      if (intersections.size() > 0) {
        if (intersections.size() > 1) {
          Collections.sort(intersections, new CoordinatesDistanceComparator(c0));
        }
        for (final Coordinates intersection : intersections) {
          if (!(index.isWithinDistance(c0) && index.isWithinDistance(c1))) {
            if (i == 1 && intersection.distance(firstCoordinate) < tolerance) {
            } else if (i == lastIndex
              && intersection.distance(lastCoordinate) < tolerance) {
            } else {
              final double d0 = intersection.distance(c0);
              final double d1 = intersection.distance(c1);
              if (d0 <= tolerance) {
                if (d1 > tolerance) {
                  addLineString(geometryFactory, points, startCoordinate,
                    startIndex, i - 1, null, newLines);
                  startIndex = i - 1;
                  startCoordinate = null;
                } else {
                  addLineString(geometryFactory, points, startCoordinate,
                    startIndex, i - 1, intersection, newLines);
                  startIndex = i + 1;
                  startCoordinate = intersection;
                  c0 = intersection;
                }
              } else if (d1 <= tolerance) {
                addLineString(geometryFactory, points, startCoordinate,
                  startIndex, i, null, newLines);
                startIndex = i;
                startCoordinate = null;
              } else {
                addLineString(geometryFactory, points, startCoordinate,
                  startIndex, i - 1, intersection, newLines);
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
      addLineString(geometryFactory, points, startCoordinate, startIndex,
        lastIndex, null, newLines);
    }
    return newLines;
  }

  public static List<LineString> split(final LineString line,
    final Coordinates point) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Map<String, Number> result = findClosestSegmentAndCoordinate(line,
      point);
    final int segmentIndex = CollectionUtil.getInteger(result, "segmentIndex");
    if (segmentIndex != -1) {
      List<LineString> lines;
      final int coordinateIndex = CollectionUtil.getInteger(result,
        "coordinateIndex");
      final double coordinateDistance = geometryFactory.makeXyPrecise(CollectionUtil.getDouble(
        result, "coordinateDistance"));
      final double segmentDistance = geometryFactory.makeXyPrecise(CollectionUtil.getDouble(
        result, "segmentDistance"));
      if (coordinateIndex == 0) {
        if (coordinateDistance == 0) {
          return Collections.singletonList(line);
        } else if (segmentDistance == 0) {
          lines = split(line, segmentIndex, point);
        } else {
          final Coordinates c0 = points.get(0);
          Coordinates c1;
          int i = 1;
          do {
            c1 = points.get(i);
            i++;
          } while (c1.equals(c0));
          if (CoordinatesUtil.isAcute(c1, c0, point)) {
            lines = split(line, 0, point);
          } else {
            return Collections.singletonList(line);
          }
        }
      } else if (coordinateIndex == line.getVertexCount() - 1) {
        if (coordinateDistance == 0) {
          return Collections.singletonList(line);
        } else if (segmentDistance == 0) {
          lines = split(line, segmentIndex, point);
        } else {
          final Coordinates cn = points.get(points.size() - 1);
          Coordinates cn1;
          int i = points.size() - 2;
          do {
            cn1 = points.get(i);
            i++;
          } while (cn1.equals(cn));
          if (CoordinatesUtil.isAcute(cn1, cn, point)) {
            lines = split(line, segmentIndex, point);
          } else {
            return Collections.singletonList(line);
          }
        }
      } else {
        lines = split(line, segmentIndex, point);
      }
      return lines;
    }
    return Collections.singletonList(line);
  }

  public static List<LineString> split(final LineString line,
    final int segmentIndex, final Coordinates point) {
    final List<LineString> lines = new ArrayList<LineString>();
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final boolean containsPoint = point.equals(points.get(segmentIndex));
    final int axisCount = points.getAxisCount();
    int coords1Size;
    int coords2Size = points.size() - segmentIndex;
    if (containsPoint) {
      coords1Size = segmentIndex + 1;
      coords2Size = points.size() - segmentIndex;
    } else {
      coords1Size = segmentIndex + 2;
      coords2Size = points.size() - segmentIndex;
    }
    final double[] coordinates1 = new double[coords1Size * axisCount];
    for (int i = 0; i < segmentIndex + 1; i++) {
      CoordinatesListUtil.setCoordinates(coordinates1, axisCount, i,
        points.get(i));
    }

    final double[] coordinates2 = new double[coords2Size * axisCount];
    if (containsPoint) {
      for (int i = 0; i < segmentIndex + 1; i++) {
        CoordinatesListUtil.setCoordinates(coordinates2, axisCount, i,
          points.get(segmentIndex + i));
      }
    } else {
      CoordinatesListUtil.setCoordinates(coordinates2, axisCount,
        coords1Size - 1, point);
      CoordinatesListUtil.setCoordinates(coordinates2, axisCount, 0, point);
      if (axisCount > 2) {
        final Coordinates previous = points.get(segmentIndex);
        final Coordinates next = points.get(segmentIndex + 1);
        final double z = getElevation(point, previous, next);
        coordinates1[(coords1Size - 1) * axisCount + 2] = z;
        coordinates2[2] = z;
      }

      for (int i = 1; i < coords2Size; i++) {
        CoordinatesListUtil.setCoordinates(coordinates2, axisCount, i,
          points.get(segmentIndex + i));
      }
    }

    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);

    if (coords1Size > 1) {
      final LineString line1 = geometryFactory.lineString(axisCount,
        coordinates1);
      if (line1.getLength() > 0) {
        lines.add(line1);
      }
    }

    if (coords2Size > 1) {
      final LineString line2 = geometryFactory.lineString(axisCount,
        coordinates2);
      if (line2.getLength() > 0) {
        lines.add(line2);
      }
    }
    return lines;
  }

  public static LineString subLineString(final LineString line,
    final Coordinates fromPoint, final int fromIndex, final int length,
    final Coordinates toPoint) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
      fromPoint, fromIndex, length, toPoint);

    final GeometryFactory factory = GeometryFactory.getFactory(line);
    if (newPoints.size() < 2) {
      return null;
    } else {
      final LineString newLine = factory.lineString(newPoints);
      GeometryProperties.copyUserData(line, newLine);
      return newLine;
    }

  }

  public static LineString subLineString(final LineString line, final int length) {
    return subLineString(line, (Coordinates)null, 0, length, null);
  }

  public static LineString subLineString(final LineString line,
    final int length, final Coordinates coordinate) {
    return subLineString(line, null, 0, length, coordinate);
  }

}
