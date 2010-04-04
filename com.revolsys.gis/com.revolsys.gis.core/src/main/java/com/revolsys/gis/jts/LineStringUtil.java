package com.revolsys.gis.jts;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public final class LineStringUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static double distance(
    final double aX1,
    final double aY1,
    final double aX2,
    final double aY2,
    final double bX1,
    final double bY1,
    final double bX2,
    final double bY2) {
    if (aX1 == aX2 && aY1 == aY2) {
      // Segment 1 is a zero length do point line distance
      return JtsGeometryUtil.distance(aX1, aY1, bX1, bY1, bX2, bY2);
    } else if (bX1 == bX2 && aY1 == bY2) {
      // Segment 2 is a zero length do point line distance
      return JtsGeometryUtil.distance(bX1, bY1, aX1, aY1, aX2, aY2);
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
        return Math.min(JtsGeometryUtil.distance(aX1, aY1, bX1, bY1, bX2, bY2),
          Math.min(JtsGeometryUtil.distance(aX2, aY2, bX1, bY1, bX2, bY2),
            Math.min(JtsGeometryUtil.distance(bX1, bY1, aX1, aY1, aX2, aY2),
              JtsGeometryUtil.distance(bX2, bY2, aX1, aY1, aX2, aY2))));

      } else {
        final double s = sTop / sBottom;
        final double r = rTop / rBottom;

        if ((r < 0) || (r > 1) || (s < 0) || (s > 1)) {
          // no intersection
          return Math.min(
            JtsGeometryUtil.distance(aX1, aY1, bX1, bY1, bX2, bY2), Math.min(
              JtsGeometryUtil.distance(aX2, aY2, bX1, bY1, bX2, bY2), Math.min(
                JtsGeometryUtil.distance(bX1, bY1, aX1, aY1, aX2, aY2),
                JtsGeometryUtil.distance(bX2, bY2, aX1, aY1, aX2, aY2))));
        }
        return 0.0;
      }
    }
  }

  public static double distance(
    final LineString line1,
    final LineString line2,
    final double terminateDistance) {
    final double envelopeDistance = line1.getEnvelopeInternal().distance(
      line2.getEnvelopeInternal());
    if (envelopeDistance > terminateDistance) {
      return Double.MAX_VALUE;
    } else {
      double minDistance = Double.MAX_VALUE;
      final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
      final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
      double previousX1 = coordinates1.getOrdinate(0, 0);
      double previousY1 = coordinates1.getOrdinate(0, 1);
      for (int i = 1; i < coordinates1.size(); i++) {
        final double x1 = coordinates1.getOrdinate(i, 0);
        final double y1 = coordinates1.getOrdinate(i, 1);

        double previousX2 = coordinates2.getOrdinate(0, 0);
        double previousY2 = coordinates2.getOrdinate(0, 1);
        for (int j = 1; j < coordinates2.size(); j++) {
          final double x2 = coordinates2.getOrdinate(j, 0);
          final double y2 = coordinates2.getOrdinate(j, 1);
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
   * @param dimension The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsExact(
    final LineString line1,
    final LineString line2,
    final int dimension) {
    if (line1 == line2) {
      return true;
    } else {
      final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
      final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
      return CoordinateSequenceUtil.equalsExact(coordinates1, coordinates2,
        dimension);
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
  public static boolean equalsExact2d(
    final LineString line1,
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
  public static boolean equalsIgnoreDirection(
    final LineString line1,
    final LineString line2,
    final int dimension) {
    if (line1 == line2) {
      return true;
    } else {
      final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
      final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
      return CoordinateSequenceUtil.equalsIgnoreDirection(coordinates1,
        coordinates2, dimension);
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
  public static boolean equalsIgnoreDirection2d(
    final LineString line1,
    final LineString line2) {
    return equalsIgnoreDirection(line1, line2, 2);
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(
    final LineString line,
    final Coordinate coordinate) {
    final Map<String, Number> result = new HashMap<String, Number>();
    result.put(SEGMENT_INDEX, -1);
    result.put(COORDINATE_INDEX, -1);
    result.put(COORDINATE_DISTANCE, Double.MAX_VALUE);
    result.put(SEGMENT_DISTANCE, Double.MAX_VALUE);
    double closestDistance = Double.MAX_VALUE;
    final CoordinateSequenceIndexLineSegmentIterator iterator = getLineSegmentIterator(line);
    if (iterator.hasNext()) {
      CoordinateSequenceIndexLineSegment segment = iterator.next();
      final double previousCoordinateDistance = segment.getStartDistance(coordinate);
      if (previousCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
        result.put(COORDINATE_INDEX, 0);
        result.put(COORDINATE_DISTANCE, 0.0);
        result.put(SEGMENT_DISTANCE, 0.0);
      } else {
        int i = 1;
        while (segment != null) {
          final double currentCoordinateDistance = segment.getEndDistance(coordinate);
          if (currentCoordinateDistance == 0) {
            result.put(SEGMENT_INDEX, i);
            result.put(COORDINATE_INDEX, i);
            result.put(COORDINATE_DISTANCE, 0.0);
            result.put(SEGMENT_DISTANCE, 0.0);
            return result;
          }
          final double distance = segment.getDistance(coordinate);
          if (distance == 0) {
            result.put(SEGMENT_INDEX, i - 1);
            result.put(SEGMENT_DISTANCE, 0.0);
            if (previousCoordinateDistance < currentCoordinateDistance) {
              result.put(COORDINATE_INDEX, i - 1);
              result.put(COORDINATE_DISTANCE, previousCoordinateDistance);
            } else {
              result.put(COORDINATE_INDEX, i);
              result.put(COORDINATE_DISTANCE, currentCoordinateDistance);
            }
            return result;
          } else if (distance < closestDistance) {
            result.put(SEGMENT_DISTANCE, distance);
            closestDistance = distance;
            result.put(SEGMENT_INDEX, i - 1);
            if (previousCoordinateDistance < currentCoordinateDistance) {
              result.put(COORDINATE_INDEX, i - 1);
              result.put(COORDINATE_DISTANCE, previousCoordinateDistance);
            } else {
              result.put(COORDINATE_INDEX, i);
              result.put(COORDINATE_DISTANCE, currentCoordinateDistance);
            }
          }
          if (iterator.hasNext()) {
            i++;
            segment = iterator.next();
          } else {
            segment = null;
          }
        }
      }
    }
    return result;
  }

  public static Coordinate getClosestCoordinateOnLineString(
    final LineString line,
    final Coordinate coordinate,
    final double tolerance) {
    final Map<String, Number> result = LineStringUtil.findClosestSegmentAndCoordinate(
      line, coordinate);
    final int segmentIndex = result.get(SEGMENT_INDEX).intValue();
    if (segmentIndex != -1) {
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      final int coordinateIndex = result.get(COORDINATE_INDEX).intValue();
      final double coordinateDistance = result.get(COORDINATE_DISTANCE)
        .doubleValue();
      final double segmentDistance = result.get(SEGMENT_DISTANCE).doubleValue();
      if (coordinateIndex == 0) {
        final Coordinate c0 = coordinates.getCoordinate(0);
        if (coordinateDistance < tolerance) {
          return c0;
        } else if (segmentDistance == 0) {
          return coordinate;
        } else {
          Coordinate c1;
          int i = 1;
          do {
            c1 = line.getCoordinateN(i);
            i++;
          } while (c1.equals(c0));
          if (Angle.isAcute(c1, c0, coordinate)) {
            final LineSegment3D lineSegment = new LineSegment3D(c0, c1);
            return lineSegment.pointAlong3D(lineSegment.segmentFraction(coordinate));
          } else {
            return c0;
          }
        }
      } else if (coordinateIndex == line.getNumPoints() - 1) {
        final Coordinate cn = coordinates.getCoordinate(coordinates.size() - 1);
        if (coordinateDistance == 0) {
          return cn;
        } else if (segmentDistance == 0) {
          return coordinate;
        } else {
          Coordinate cn1;
          int i = line.getNumPoints() - 2;
          do {
            cn1 = line.getCoordinateN(i);
            i++;
          } while (cn1.equals(cn));
          if (Angle.isAcute(cn1, cn, coordinate)) {
            final LineSegment3D lineSegment = new LineSegment3D(cn1, cn);
            return lineSegment.pointAlong3D(lineSegment.segmentFraction(coordinate));
          } else {
            return cn;
          }
        }
      } else {
        final Coordinate cn1 = coordinates.getCoordinate(coordinateIndex - 1);
        final double cn1Distance = coordinate.distance(cn1);
        final Coordinate cn2 = coordinates.getCoordinate(coordinateIndex);
        final double cn2Distance = coordinate.distance(cn2);
        if (cn1Distance < cn2Distance) {
          if (cn1Distance < tolerance) {
            return cn1;
          }
        } else if (cn2Distance < tolerance) {
          return cn2;
        }
        final LineSegment3D lineSegment = new LineSegment3D(cn1, cn2);
        return lineSegment.pointAlong3D(lineSegment.segmentFraction(coordinate));
      }
    }
    return null;
  }

  public static CoordinateSequenceIndexLineSegmentIterator getLineSegmentIterator(
    final LineString line) {
    return new CoordinateSequenceIndexLineSegmentIterator(
      line.getCoordinateSequence());
  }

  public static boolean hasEqualExact2d(
    final List<LineString> lines,
    final LineString newLine) {
    for (final LineString line : lines) {
      if (equalsExact2d(line, newLine)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLoop(
    final Collection<LineString> lines) {
    for (final LineString line : lines) {
      if (isClosed(line)) {
        return true;
      }
    }
    return false;

  }

  /**
   * Create a new line by inserting the list of coordinates into the line.
   * 
   * @param line The line to insert the coordinates into.
   * @param coordinates The coordinates.
   * @return The new line.
   */
  public static LineString insert(
    final LineString line,
    final List<Coordinate> coordinates) {
    final SpatialIndex index = new Quadtree();
    final List<LineSegment> segments = new LinkedList<LineSegment>();

    for (final CoordinateSequenceIndexLineSegment segment : getLineSegmentIterator(line)) {
      final LineSegment newSegment = new SimpleLineSegment(segment);
      index.insert(newSegment.getEnvelope(), newSegment);
      segments.add(newSegment);
    }

    for (final Coordinate coordinate : coordinates) {
      final Envelope envelope = new Envelope(coordinate);
      envelope.expandBy(2);
      final List<LineSegment> matchedSegments = index.query(envelope);
      LineSegment matchedLineSegment = null;
      double matchedDistance = Double.MAX_VALUE;
      for (final LineSegment lineSegment : matchedSegments) {
        final double distance = lineSegment.getDistance(coordinate);
        if (distance < 2) {
          if (matchedLineSegment == null || distance < matchedDistance) {
            matchedLineSegment = lineSegment;
            matchedDistance = distance;
          }
        }
      }
      if (matchedLineSegment != null) {
        if (!matchedLineSegment.contains(coordinate)) {
          final ListIterator<LineSegment> segmentIter = segments.listIterator();
          if (segmentIter.hasNext()) {
            LineSegment segment = segmentIter.next();
            while (segment != matchedLineSegment && segmentIter.hasNext()) {
              segment = segmentIter.next();
            }
            if (segment == matchedLineSegment) {
              segmentIter.remove();
            }
          }

          index.remove(matchedLineSegment.getEnvelope(), matchedLineSegment);
          final double[] coordinatesCoordinates = new double[] {
            coordinate.x, coordinate.y, coordinate.z
          };
          final LineSegment segment1 = new SimpleLineSegment(
            matchedLineSegment.getStartCoordinates(), coordinatesCoordinates);
          index.insert(segment1.getEnvelope(), segment1);
          segmentIter.add(segment1);

          final LineSegment segment2 = new SimpleLineSegment(
            coordinatesCoordinates, matchedLineSegment.getEndCoordinates());
          index.insert(segment2.getEnvelope(), segment2);
          segmentIter.add(segment2);
        }
      }
    }
    final int dimension = line.getCoordinateSequence().getDimension();
    final CoordinateSequence newCoordinates = new DoubleCoordinatesList(
      segments.size() + 1, dimension);
    for (int o = 0; o < dimension; o++) {
      final LineSegment firstSegment = segments.get(0);
      final double ordinate = firstSegment.getStartOrdinate(o);
      newCoordinates.setOrdinate(0, o, ordinate);
    }
    int i = 1;
    for (final LineSegment lineSegment : segments) {
      for (int o = 0; o < dimension; o++) {
        final double ordinate = lineSegment.getEndOrdinate(o);
        newCoordinates.setOrdinate(i, o, ordinate);
      }
      i++;
    }
    final LineString newLine = line.getFactory().createLineString(
      newCoordinates);
    return newLine;
  }

  public static boolean isCCW(
    final LineString line) {
    if (line == null) {
      return false;
    } else {
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      return CoordinateSequenceUtil.isCCW(coordinates);
    }
  }

  public static boolean isClosed(
    final LineString line) {
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    final int lastCoordinateIndex = coordinates.size() - 1;
    if (coordinates.getOrdinate(0, 0) == coordinates.getOrdinate(
      lastCoordinateIndex, 0)) {
      if (coordinates.getOrdinate(0, 1) == coordinates.getOrdinate(
        lastCoordinateIndex, 1)) {
        return true;
      }
    }
    return false;
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
  public static LineString merge(
    final LineString line1,
    final LineString line2) {
    final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
    final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
    final CoordinateSequence coordinates = CoordinateSequenceUtil.merge(
      coordinates1, coordinates2);
    final GeometryFactory factory = line1.getFactory();
    final LineString line = factory.createLineString(coordinates);
    line.setUserData(line1.getUserData());
    return line;
  }

  public static LineString reverse(
    final LineString line) {
    final GeometryFactory factory = line.getFactory();
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    final CoordinatesList reverseCoordinates = CoordinateSequenceUtil.reverse(coordinates);
    final LineString newLine = factory.createLineString(reverseCoordinates);
    JtsGeometryUtil.copyUserData(line, newLine);
    return newLine;
  }

  public static LineString subLineString(
    final LineString line,
    final Coordinate fromCoordinate,
    final int fromIndex,
    final int length,
    final Coordinate toCoordinate) {
    final CoordinateSequence coords = line.getCoordinateSequence();
    final CoordinateSequence newCoords = CoordinateSequenceUtil.subSequence(
      coords, fromCoordinate, fromIndex, length, toCoordinate);
    final GeometryFactory factory = line.getFactory();
    final LineString newLine = factory.createLineString(newCoords);
    final Map<String, Object> userData = JtsGeometryUtil.getGeometryProperties(line);
    newLine.setUserData(userData);
    return newLine;

  }

  public static LineString subLineString(
    final LineString line,
    final int length) {
    return subLineString(line, null, 0, length, null);
  }

  public static LineString subLineString(
    final LineString line,
    final int length,
    final Coordinate coordinate) {
    return subLineString(line, null, 0, length, coordinate);
  }

  public static double getLength(
    MultiLineString lines) {
    double length = 0;
    for (int i = 0; i < lines.getNumGeometries(); i++) {
      LineString line = (LineString)lines.getGeometryN(i);
      length += line.getLength();
    }
    return length;
  }
}
