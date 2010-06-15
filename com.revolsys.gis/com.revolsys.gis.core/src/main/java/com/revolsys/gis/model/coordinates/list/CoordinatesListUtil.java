package com.revolsys.gis.model.coordinates.list;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class CoordinatesListUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

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
      final double previousCoordinateDistance = segment.getPoint(0).distance(point);
      if (previousCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
        result.put(COORDINATE_INDEX, 0);
        result.put(COORDINATE_DISTANCE, 0.0);
        result.put(SEGMENT_DISTANCE, 0.0);
      } else {
        int i = 1;
        while (segment != null) {
          final double currentCoordinateDistance = segment.getPoint(1).distance(point);
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

  public static final CoordinatesList get(
    final CoordinateSequence coordinateSequence) {
    if (coordinateSequence instanceof CoordinatesList) {
      return (CoordinatesList)coordinateSequence;
    } else {
      return new CoordinateSequenceCoordinateList(coordinateSequence);
    }

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
    final Coordinates coordinates1Start = coordinates1.getPoint(0);
    final Coordinates coordinates1End = coordinates1.getPoint(coordinates1.size() - 1);
    final Coordinates coordinates2Start = coordinates2.getPoint(0);
    final Coordinates coordinates2End = coordinates2.getPoint(coordinates2.size() - 1);
    DataObjectUtil.noopOnCoordinateEqual2d(coordinates1End, 1335445.127, 1637621.46);
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
      throw new IllegalArgumentException("lines don't touch");

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
    if (startPoint != null) {
      size++;
      lastIndex++;
      startIndex++;
    }
    if (endPoint != null) {
      size++;
    }
    final CoordinatesList newPoints = new DoubleCoordinatesList(size, dimension);

    if (startPoint != null) {
      newPoints.setPoint(0, startPoint);
    }

    points.copy(start, newPoints, startIndex, dimension, length);

    if (endPoint != null) {
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

  public static CoordinatesList get(
    LineString line) {
    return get(line.getCoordinateSequence());
  }

  public static CoordinatesList get(
    Point point) {
    return get(point.getCoordinateSequence());
  }

  public static CoordinatesList create(
    List<Coordinates> coordinateArray,
    int numAxis) {
    CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinateArray.size(), numAxis);
    int i = 0;
    for (Coordinates coordinates : coordinateArray) {
      if (coordinates != null) {
        for (int j = 0; j < coordinates.getNumAxis(); j++) {
          coordinatesList.setOrdinate(i, j, coordinates.getValue(j));
        }
        i++;
      }
    }
    if (i < coordinatesList.size()) {
      coordinatesList = coordinatesList.subList(0, i);
    }
    return coordinatesList;
  }
}
