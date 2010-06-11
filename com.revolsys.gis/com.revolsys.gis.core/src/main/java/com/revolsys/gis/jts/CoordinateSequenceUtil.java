package com.revolsys.gis.jts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

/**
 * The CoordinateSequenceUtil class provides utility methods for processing
 * {@link CoordinateSequence} classes.
 * 
 * @author Paul Austin
 */
public final class CoordinateSequenceUtil {

  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

 


  /**
   * Copy length coordinates in src staring at srcPos to dest at destPo. This
   * method silently truncates the number dimension if the src or dest does not
   * have that many coordinates
   * 
   * @param src The source coordinates.
   * @param srcPos The source position.
   * @param dest The destination coordinates.
   * @param destPos The destination position.
   * @param length The number of coordinates to copy.
   */
  public static void copy(
    final CoordinateSequence src,
    final int srcPos,
    final CoordinateSequence dest,
    final int destPos,
    final int length) {
    final int minDimension = Math.min(src.getDimension(), dest.getDimension());
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < minDimension; j++) {
        final double ordinate = src.getOrdinate(srcPos + i, j);
        dest.setOrdinate(destPos + i, j, ordinate);
      }
    }
  }

  public static boolean equals2dCoordinate(
    final CoordinateSequence coordinates,
    final int index,
    final double x,
    final double y) {
    return coordinates.getOrdinate(index, 0) == x
      && coordinates.getOrdinate(index, 1) == y;
  }

  public static boolean equals2dCoordinates(
    final CoordinateSequence coordinates,
    final int index1,
    final int index2) {
    return coordinates.getOrdinate(index1, 0) == coordinates.getOrdinate(
      index2, 0)
      && coordinates.getOrdinate(index1, 1) == coordinates.getOrdinate(index2,
        1);
  }



  /**
   * Compare the coordinates of the two coordinate sequences up to the given
   * dimension to see if they have the same ordinate values.
   * 
   * @param coordinates1 The first coordinates.
   * @param coordinates2 The second coordinates.
   * @param dimension The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsExact(
    final CoordinateSequence coordinates1,
    final CoordinateSequence coordinates2,
    final int dimension) {
    if (coordinates1 == coordinates2) {
      return true;
    } else {
      if (coordinates1.size() == coordinates2.size()) {
        final int dimension1 = coordinates1.getDimension();
        final int dimension2 = coordinates2.getDimension();

        int compareDimension = dimension;
        if (dimension1 == dimension2) {
          compareDimension = Math.min(compareDimension, dimension1);
        }

        if (dimension1 >= compareDimension) {
          if (dimension2 >= compareDimension) {
            for (int i = 0; i < coordinates1.size(); i++) {
              for (int j = 0; j < compareDimension; j++) {
                final double ordinate1 = coordinates1.getOrdinate(i, j);
                final double ordinate2 = coordinates2.getOrdinate(i, j);
                if (Double.compare(ordinate1, ordinate2) != 0) {
                  return false;
                }
              }
            }
            return true;
          }
        }
      }
      return false;
    }
  }

  /**
   * Compare the 2D coordinates of the two coordinate sequences to see if they
   * have the same ordinate values.
   * 
   * @param coordinates1 The first coordinates.
   * @param coordinates2 The second coordinates.
   * @return True if the coordinates match.
   */
  public static boolean equalsExact2d(
    final CoordinateSequence coordinates1,
    final CoordinateSequence coordinates2) {
    return equalsExact(coordinates1, coordinates2, 2);
  }

  /**
   * Compare the coordinates of the two coordinate sequences up to the given
   * dimension to see if they have the same ordinate values in either the
   * forward or reverse direction.
   * 
   * @param coordinates1 The first coordinates.
   * @param coordinates2 The second coordinates.
   * @param dimension The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection(
    final CoordinateSequence coordinates1,
    final CoordinateSequence coordinates2,
    final int dimension) {
    if (equalsExact(coordinates1, coordinates2, dimension)) {
      return true;
    } else {
      final CoordinatesList reverseCoordinates2 = CoordinateSequenceUtil.reverse(coordinates2);
      return equalsExact(coordinates1, reverseCoordinates2, dimension);
    }
  }

  /**
   * Compare the 2D coordinates of the two coordinate sequences to see if they
   * have the same ordinate values in either the forward or reverse direction.
   * 
   * @param coordinates1 The first coordinates.
   * @param coordinates2 The second coordinates.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection2d(
    final CoordinateSequence coordinates1,
    final CoordinateSequence coordinates2) {
    return equalsIgnoreDirection(coordinates1, coordinates2, 2);
  }

  public static boolean equalsOrdinate(
    final CoordinateSequence coordinates,
    final int index,
    final int ordinateIndex,
    final double ordinate) {
    return ordinate == coordinates.getOrdinate(index, ordinateIndex);
  }

  public static boolean equalsOrdinate(
    final CoordinateSequence coordinates,
    final int index1,
    final int index2,
    final int ordinateIndex) {
    final double ordinate1 = coordinates.getOrdinate(index1, ordinateIndex);
    final double ordinate2 = coordinates.getOrdinate(index2, ordinateIndex);
    return ordinate1 == ordinate2;
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(
    final CoordinateSequence coordinates,
    final Coordinate coordinate) {
    final Map<String, Number> result = new HashMap<String, Number>();
    result.put(SEGMENT_INDEX, -1);
    result.put(COORDINATE_INDEX, -1);
    result.put(COORDINATE_DISTANCE, -1);
    result.put(SEGMENT_DISTANCE, -1);
    double closestDistance = Double.MAX_VALUE;
    final CoordinateSequenceIndexLineSegmentIterator iterator = getLineSegmentIterator(coordinates);
    if (iterator.hasNext()) {
      CoordinateSequenceIndexLineSegment segment = iterator.next();
      final double previousCoordinateDistance = segment.getStartDistance(coordinate);
      if (previousCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
        result.put(COORDINATE_INDEX, 0);
        result.put(COORDINATE_DISTANCE, 0);
        result.put(SEGMENT_DISTANCE, 0);
      } else {
        int i = 1;
        while (segment != null) {
          final double currentCoordinateDistance = segment.getEndDistance(coordinate);
          if (currentCoordinateDistance == 0) {
            result.put(SEGMENT_INDEX, i);
            result.put(COORDINATE_INDEX, i);
            result.put(COORDINATE_DISTANCE, 0);
            result.put(SEGMENT_DISTANCE, 0);
            return result;
          }
          final double distance = segment.getDistance(coordinate);
          if (distance == 0) {
            result.put(SEGMENT_INDEX, i - 1);
            result.put(SEGMENT_DISTANCE, 0);
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

  public static CoordinateSequenceIndexLineSegmentIterator getLineSegmentIterator(
    final CoordinateSequence coordinateSequence) {
    return new CoordinateSequenceIndexLineSegmentIterator(coordinateSequence);
  }

  public static boolean isCCW(
    final CoordinateSequence ring) {
    // # of points without closing endpoint
    final int nPts = ring.size() - 1;

    // find highest point
    double hiPtX = ring.getOrdinate(0, 0);
    double hiPtY = ring.getOrdinate(0, 1);
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final double x = ring.getOrdinate(i, 0);
      final double y = ring.getOrdinate(i, 1);
      if (y > hiPtY) {
        hiPtX = x;
        hiPtY = y;
        hiIndex = i;
      }
    }

    // find distinct point before highest point
    int iPrev = hiIndex;
    do {
      iPrev = iPrev - 1;
      if (iPrev < 0) {
        iPrev = nPts;
      }
    } while (equals2dCoordinate(ring, iPrev, hiPtX, hiPtY) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % nPts;
    } while (equals2dCoordinate(ring, iNext, hiPtX, hiPtY) && iNext != hiIndex);

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (equals2dCoordinate(ring, iPrev, hiPtX, hiPtY)
      || equals2dCoordinate(ring, iNext, hiPtX, hiPtY)
      || equals2dCoordinates(ring, iPrev, iNext)) {
      return false;
    }

    final int disc = orientationIndex(ring, iPrev, hiIndex, iNext);

    /**
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the lines
     * lie on top of one another (1) is handled by checking if next is left of
     * prev ==> CCW (2) will never happen if the ring is valid, so don't check
     * for it (Might want to assert this)
     */
    boolean isCCW = false;
    if (disc == 0) {
      // poly is CCW if prev x is right of next x
      isCCW = (ring.getOrdinate(iPrev, 0) > ring.getOrdinate(iPrev, 1));
    } else {
      // if area is positive, points are ordered CCW
      isCCW = (disc > 0);
    }
    return isCCW;
  }

  public static int orientationIndex(
    final CoordinateSequence ring,
    final int index1,
    final int index2,
    final int index) {
    // TODO Auto-generated method stub
    return orientationIndex(ring.getOrdinate(index1, 0), ring.getOrdinate(
      index1, 1), ring.getOrdinate(index2, 0), ring.getOrdinate(index2, 1),
      ring.getOrdinate(index, 0), ring.getOrdinate(index, 1));
  }

  /**
   * Returns the index of the direction of the point <code>q</code> relative to
   * a vector specified by <code>p1-p2</code>.
   * 
   * @param p1 the origin point of the vector
   * @param p2 the final point of the vector
   * @param q the point to compute the direction to
   * @return 1 if q is counter-clockwise (left) from p1-p2
   * @return -1 if q is clockwise (right) from p1-p2
   * @return 0 if q is collinear with p1-p2
   */
  public static int orientationIndex(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x,
    final double y) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double dx1 = x2 - x1;
    final double dy1 = y2 - y1;
    final double dx2 = x - x2;
    final double dy2 = y - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static CoordinatesList reverse(
    final CoordinateSequence coordinateSequence) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(coordinateSequence);
    return coordinatesList.reverse();
  }

  public static CoordinateSequence subSequence(
    final CoordinateSequence coords,
    final Coordinate startCoordinate,
    final int start,
    final int length) {
    final int dimension = coords.getDimension();
    final CoordinateSequence newCoords = new DoubleCoordinatesList(length + 1,
      dimension);

    newCoords.setOrdinate(0, 0, startCoordinate.x);
    newCoords.setOrdinate(0, 1, startCoordinate.y);
    if (dimension > 2) {
      newCoords.setOrdinate(0, 2, startCoordinate.z);
    }

    copy(coords, start, newCoords, 1, length);

    return newCoords;
  }

  public static CoordinateSequence subSequence(
    final CoordinateSequence coords,
    final Coordinate startCoordinate,
    final int start,
    final int length,
    final Coordinate endCoordinate) {
    final int dimension = coords.getDimension();
    int size = length;
    int startIndex = 0;
    int lastIndex = length;
    if (startCoordinate != null) {
      size++;
      lastIndex++;
      startIndex++;
    }
    if (endCoordinate != null) {
      size++;
    }
    final CoordinateSequence newCoords = new DoubleCoordinatesList(size,
      dimension);

    if (startCoordinate != null) {
      newCoords.setOrdinate(0, 0, startCoordinate.x);
      newCoords.setOrdinate(0, 1, startCoordinate.y);
      if (dimension > 2) {
        newCoords.setOrdinate(0, 2, startCoordinate.z);
      }
    }

    copy(coords, start, newCoords, startIndex, length);

    if (endCoordinate != null) {
      newCoords.setOrdinate(lastIndex, 0, endCoordinate.x);
      newCoords.setOrdinate(lastIndex, 1, endCoordinate.y);
      if (dimension > 2) {
        newCoords.setOrdinate(lastIndex, 2, endCoordinate.z);
      }
    }

    return newCoords;
  }

  public static CoordinateSequence subSequence(
    final CoordinateSequence coords,
    final int start,
    final int length) {
    final int dimension = coords.getDimension();
    final CoordinateSequence newCoords = new DoubleCoordinatesList(length,
      dimension);
    copy(coords, start, newCoords, 0, length);
    return newCoords;
  }

  public static CoordinateSequence subSequence(
    final CoordinateSequence coords,
    final int start,
    final int length,
    final Coordinate endCoordinate) {
    final int dimension = coords.getDimension();
    final CoordinateSequence newCoords = new DoubleCoordinatesList(length + 1,
      dimension);

    copy(coords, start, newCoords, 0, length);

    newCoords.setOrdinate(length, 0, endCoordinate.x);
    newCoords.setOrdinate(length, 1, endCoordinate.y);
    if (dimension > 2) {
      newCoords.setOrdinate(length, 2, endCoordinate.z);
    }

    return newCoords;
  }

  public static CoordinateSequence trim(
    final CoordinateSequence coordinates,
    final int length) {
    if (length == coordinates.size()) {
      return coordinates;
    } else {
      final int dimension = coordinates.getDimension();
      final CoordinateSequence newCoordinates = new DoubleCoordinatesList(
        length, dimension);
      copy(coordinates, 0, newCoordinates, 0, length);
      return newCoordinates;
    }
  }

  /**
   * Construct a new
   */
  private CoordinateSequenceUtil() {
  }
}
