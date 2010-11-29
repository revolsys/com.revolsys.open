package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class CoordinatesListUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static void addElevation(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates coordinate,
    final CoordinatesList line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesListCoordinates previousCoordinate = new CoordinatesListCoordinates(
      points, 0);
    final CoordinatesListCoordinates currentCoordinate = new CoordinatesListCoordinates(
      points, 0);
    for (int i = 1; i < points.size(); i++) {
      currentCoordinate.next();

      if (LineSegmentUtil.isPointOnLine(precisionModel, previousCoordinate,
        currentCoordinate, coordinate)) {
        LineSegmentUtil.addElevation(precisionModel, previousCoordinate,
          currentCoordinate, coordinate);
        return;
      }
      previousCoordinate.next();
    }

  }

  public static int append(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getX(startIndex - 1);
      previousY = dest.getY(startIndex - 1);
    }
    for (int i = 0; i < srcSize && coordIndex < destSize; i++) {
      final double x = src.getX(i);
      final double y = src.getY(i);
      if (x != previousX || y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static int appendReversed(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getX(startIndex - 1);
      previousY = dest.getY(startIndex - 1);
    }
    for (int i = srcSize - 1; i > -1 && coordIndex < destSize; i--) {
      final double x = src.getX(i);
      final double y = src.getY(i);
      if (x != previousX || y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static CoordinatesList create(
    final List<Coordinates> coordinateArray,
    final int numAxis) {
    CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinateArray.size(), numAxis);
    int i = 0;
    for (final Coordinates coordinates : coordinateArray) {
      if (coordinates != null) {
        for (int j = 0; j < coordinates.getNumAxis(); j++) {
          coordinatesList.setValue(i, j, coordinates.getValue(j));
        }
        i++;
      }
    }
    if (i < coordinatesList.size()) {
      coordinatesList = coordinatesList.subList(0, i);
    }
    return coordinatesList;
  }

  public static boolean equals2dCoordinate(
    final CoordinatesList coordinates,
    final int index,
    final double x,
    final double y) {
    return coordinates.getX(index) == x && coordinates.getY(index) == y;
  }

  public static boolean equals2dCoordinates(
    final CoordinatesList coordinates,
    final int index1,
    final int index2) {
    return coordinates.getX(index1) == coordinates.getOrdinate(index2, 0)
      && coordinates.getY(index1) == coordinates.getOrdinate(index2, 1);
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(
    final CoordinatesList points,
    final Coordinates point) {
    final Map<String, Number> result = new HashMap<String, Number>();
    result.put(SEGMENT_INDEX, -1);
    result.put(COORDINATE_INDEX, -1);
    result.put(COORDINATE_DISTANCE, Double.MAX_VALUE);
    result.put(SEGMENT_DISTANCE, Double.MAX_VALUE);
    double closestDistance = Double.MAX_VALUE;
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      points);
    if (iterator.hasNext()) {
      LineSegment segment = iterator.next();
      final double previousCoordinateDistance = segment.get(0).distance(point);
      if (previousCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
        result.put(COORDINATE_INDEX, 0);
        result.put(COORDINATE_DISTANCE, 0.0);
        result.put(SEGMENT_DISTANCE, 0.0);
      } else {
        int i = 1;

        while (segment != null) {
          final double currentCoordinateDistance = segment.get(1).distance(
            point);
          if (currentCoordinateDistance == 0) {
            result.put(SEGMENT_INDEX, i);
            result.put(COORDINATE_INDEX, i);
            result.put(COORDINATE_DISTANCE, 0.0);
            result.put(SEGMENT_DISTANCE, 0.0);
            return result;
          }
          final double distance = segment.distance(point);
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

  public static final CoordinatesList get(
    final CoordinateSequence coordinateSequence) {
    if (coordinateSequence instanceof CoordinatesList) {
      return (CoordinatesList)coordinateSequence;
    } else {
      return new CoordinateSequenceCoordinateList(coordinateSequence);
    }

  }

  public static CoordinatesList get(
    final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else if (geometry instanceof Point) {
      return get((Point)geometry);
    } else if (geometry instanceof LineString) {
      return get((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return get(polygon);
    } else if (geometry.getNumGeometries() > 0) {
      return get(geometry.getGeometryN(0));
    } else {
      return null;
    }
  }

  public static CoordinatesList get(
    final LineString line) {
    return get(line.getCoordinateSequence());
  }

  public static CoordinatesList get(
    final Point point) {
    return get(point.getCoordinateSequence());
  }

  private static CoordinatesList get(
    final Polygon polygon) {
    if (polygon == null) {
      return null;
    } else {
      return get(polygon.getExteriorRing());
    }
  }

  public static boolean isCCW(
    final CoordinatesList ring) {
    // # of points without closing endpoint
    final int nPts = ring.size() - 1;

    // find highest point
    double hiPtX = ring.getX(0);
    double hiPtY = ring.getY(0);
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final double x = ring.getX(i);
      final double y = ring.getY(i);
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

  public static double length2d(
    final CoordinatesList points) {
    double length = 0;
    final int size = points.size();
    if (size > 1) {
      double x1 = points.getX(0);
      double y1 = points.getY(0);
      for (int i = 1; i < size; i++) {
        final double x2 = points.getX(i);
        final double y2 = points.getY(i);
        length += MathUtil.distance(x1, y1, x2, y2);
        x1 = x2;
        y1 = y2;
      }
    }
    return length;
  }

  public static CoordinatesList merge(
    final CoordinatesList coordinates1,
    final CoordinatesList coordinates2) {
    final int dimension = Math.max(coordinates1.getDimension(),
      coordinates2.getDimension());
    final int maxSize = coordinates1.size() + coordinates2.size();
    final CoordinatesList coordinates = new DoubleCoordinatesList(maxSize,
      dimension);

    int numCoords = 0;
    final Coordinates coordinates1Start = coordinates1.get(0);
    final Coordinates coordinates1End = coordinates1.get(coordinates1.size() - 1);
    final Coordinates coordinates2Start = coordinates2.get(0);
    final Coordinates coordinates2End = coordinates2.get(coordinates2.size() - 1);
    if (coordinates1Start.equals2d(coordinates2End)) {
      numCoords = append(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates2Start.equals2d(coordinates1End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = append(coordinates2, coordinates, numCoords);
    } else if (coordinates1Start.equals2d(coordinates2Start)) {
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates1End.equals2d(coordinates2End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + coordinates1
        + "\n" + coordinates2);

    }
    return trim(coordinates, numCoords);
  }

  public static CoordinatesList merge(
    final List<CoordinatesList> coordinatesList) {
    final Iterator<CoordinatesList> iterator = coordinatesList.iterator();
    if (!iterator.hasNext()) {
      return null;
    } else {
      CoordinatesList coordinates = iterator.next();
      while (iterator.hasNext()) {
        final CoordinatesList nextCoordinates = iterator.next();
        coordinates = merge(coordinates, nextCoordinates);
      }
      return coordinates;
    }
  }

  public static int orientationIndex(
    final CoordinatesList ring,
    final int index1,
    final int index2,
    final int index) {
    return orientationIndex(ring.getX(index1), ring.getY(index1),
      ring.getX(index2), ring.getY(index2), ring.getX(index), ring.getY(index));
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

  public static final CoordinatesList parse(
    final String value,
    final String separator,
    final int numAxis) {
    final String[] values = value.split(separator);
    final double[] coordinates = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final String string = values[i];
      coordinates[i] = Double.parseDouble(string);
    }
    return new DoubleCoordinatesList(numAxis, coordinates);
  }

  public static CoordinatesList subList(
    final CoordinatesList points,
    final Coordinates startPoint,
    final int start,
    final int length,
    final Coordinates endPoint) {
    final int dimension = points.getNumAxis();
    int size = length;
    int startIndex = 0;
    int lastIndex = length;
    boolean startEqual = false;
    boolean endEqual = false;
    if (startPoint != null) {
      startEqual = startPoint.equals2d(points.get(start));
      if (!startEqual) {
        size++;
        lastIndex++;
        startIndex++;
      }
    }
    if (endPoint != null) {
      endEqual = endPoint.equals2d(points.get(start + length - 1));
      if (!endEqual) {
        size++;
      }
    }
    final CoordinatesList newPoints = new DoubleCoordinatesList(size, dimension);

    if (!startEqual && startPoint != null) {
      newPoints.setPoint(0, startPoint);
    }

    points.copy(start, newPoints, startIndex, dimension, length);

    if (!endEqual && endPoint != null) {
      newPoints.setPoint(lastIndex, endPoint);
    }

    return newPoints;
  }

  public static CoordinatesList trim(
    final CoordinatesList coordinates,
    final int length) {
    if (length == coordinates.size()) {
      return coordinates;
    } else {
      return coordinates.subList(0, length);
    }
  }

  public static CoordinatesList parse(
    String value,
    String decimal,
    String coordSeperator,
    String toupleSeperator) {

    toupleSeperator = toupleSeperator.replaceAll("\\\\","\\\\\\\\");
    toupleSeperator = toupleSeperator.replaceAll("\\.", "\\\\.");
    Pattern touplePattern = Pattern.compile("\\s*" + toupleSeperator + "\\s*");
    String[] touples = touplePattern.split(value);

    coordSeperator = coordSeperator.replaceAll("\\\\","\\\\\\\\");
    coordSeperator = coordSeperator.replaceAll("\\.", "\\\\.");
    Pattern coordinatePattern = Pattern.compile("\\s*" + coordSeperator
      + "\\s*");

    int numAxis = 0;
    List<double[]> listOfCoordinateArrays = new ArrayList<double[]>();
    if (touples.length == 0) {
      return null;
    } else {
      for (String touple : touples) {
        final String[] values = coordinatePattern.split(touple);
        if (values.length > 0) {
          double[] coordinates = MathUtil.toDoubleArray(values);
          numAxis  =Math.max(numAxis, coordinates.length);
          listOfCoordinateArrays.add(coordinates);
        }
      }
    }
    

    return toCoordinateList(numAxis,listOfCoordinateArrays);
  }

  public static CoordinatesList toCoordinateList(
    int numAxis, List<double[]> listOfCoordinateArrays) {
    CoordinatesList points = new DoubleCoordinatesList(listOfCoordinateArrays.size(), numAxis);
    for (int i = 0; i < listOfCoordinateArrays.size(); i++) {
      double[]coordinates = listOfCoordinateArrays.get(i);
      for (int j = 0; j < coordinates.length; j++) {
        double value = coordinates[j];
        points.setValue(i, j, value);
      }
    }
    return points;
  }
}
