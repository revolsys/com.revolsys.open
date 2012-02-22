package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class PackedCoordinateUtil {
  public static List<CoordinatesList> getCoordinatesLists(
    final int numPoints,
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final Double zOffset,
    final Double zScale,
    final Double mOffset,
    final Double mScale,
    final byte[] packedCoordinates) {
    final InputStream inputStream = new ByteArrayInputStream(packedCoordinates);
    return getCoordinatesLists(numPoints, xOffset, yOffset, xyScale, zOffset,
      zScale, mOffset, mScale, inputStream);
  }

  public static List<CoordinatesList> getCoordinatesLists(
    final int numPoints,
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final Double zOffset,
    final Double zScale,
    final Double mOffset,
    final Double mScale,
    final InputStream inputStream) {

    try {
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      final PackedIntegerInputStream in = new PackedIntegerInputStream(
        inputStream);

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int numAxis;
      if (hasM) {
        numAxis = 4;
      } else if (hasZ) {
        numAxis = 3;
      } else {
        numAxis = 2;
      }
      final DoubleCoordinatesList points = new DoubleCoordinatesList(numPoints,
        numAxis);

      double x = xOffset;
      double y = yOffset;

      int j = 0;
      for (int i = 0; i < numPoints; i++) {
        x = readOordinate(in, points, j, 0, x, xyScale);
        y = readOordinate(in, points, j, 1, y, xyScale);
        if (x == -1 && y == 0) {
          pointsList.add(points.subList(0, j));
          j = 0;
        } else {
          j++;
        }
      }
      pointsList.add(points.subList(0, j));

      if (hasZ) {
        readOordinates(in, pointsList, 2, zOffset, zScale);
      }
      if (hasM) {
        readOordinates(in, pointsList, 3, mOffset, mScale);
      }

      return pointsList;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  public static Geometry getGeometry(
    final byte[] data,
    final GeometryFactory geometryFactory,
    final int entity,
    final int numPoints,
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final Double zOffset,
    final Double zScale,
    final Double mOffset,
    final Double mScale) {
    final InputStream in = new ByteArrayInputStream(data);
    return getGeometry(in, geometryFactory, entity, numPoints, xOffset,
      yOffset, xyScale, zOffset, zScale, mOffset, mScale);
  }

  public static Geometry getGeometry(
    final InputStream pointsIn,
    final GeometryFactory geometryFactory,
    final int entity,
    final int numPoints,
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final Double zOffset,
    final Double zScale,
    final Double mOffset,
    final Double mScale) {
    final Geometry geometry;
    final List<CoordinatesList> pointsList;
    switch (entity) {
      case ArcSdeConstants.ST_GEOMETRY_POINT:
        pointsList = getCoordinatesLists(numPoints, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale, pointsIn);
        geometry = geometryFactory.createPoint(pointsList.get(0));
      break;
      case ArcSdeConstants.ST_GEOMETRY_LINESTRING:
        pointsList = getCoordinatesLists(numPoints, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale, pointsIn);
        geometry = geometryFactory.createLineString(pointsList.get(0));
      break;
      case ArcSdeConstants.ST_GEOMETRY_POLYGON:
        pointsList = getCoordinatesLists(numPoints, xOffset, yOffset, xyScale,
          zOffset, zScale, mOffset, mScale, pointsIn);
        geometry = geometryFactory.createPolygon(pointsList);
      break;
      // TODO multi geometries
      default:
        throw new IllegalArgumentException("Unknown ST_GEOMETRY entity type: "
          + entity);
    }
    return geometry;
  }

  public static int getNumPoints(final List<CoordinatesList> pointsList) {
    int numCoordinates = pointsList.size();
    if (numCoordinates > 0) {
      numCoordinates--;
    }
    for (final CoordinatesList points : pointsList) {
      numCoordinates += points.size();
    }
    return numCoordinates;
  }

  public static byte[] getPackedBytes(
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final boolean hasZ,
    final Double zOffset,
    final Double zScale,
    final boolean hasM,
    final Double mScale,
    final Double mOffset,
    final Geometry geometry) {
    final List<CoordinatesList> pointsList = getPointsList(geometry);
    final int numCoordinates = getNumPoints(pointsList);
    return getPackedBytes(xOffset, yOffset, xyScale, hasZ, zOffset, zScale,
      hasM, mOffset, mScale, pointsList, numCoordinates);
  }

  public static byte[] getPackedBytes(
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final boolean hasZ,
    final Double zOffset,
    final Double zScale,
    final boolean hasM,
    final Double mScale,
    final Double mOffset,
    final List<CoordinatesList> pointsList, int numCoordinates) {
    

    final int packedByteLength = 0;
    byte dimensionFlag = 0;
    final byte annotationDimension = 0;
    final byte shapeFlags = 0;

    int numAxis = 2;

    if (hasZ) {
      dimensionFlag |= 1;
      numAxis++;
    }
    if (hasM) {
      dimensionFlag |= 2;
      numAxis++;
    }

    final PackedIntegerOutputStream out = new PackedIntegerOutputStream(
      numCoordinates, numAxis);
    out.writeLong5(packedByteLength);
    out.writeLong(dimensionFlag);
    out.writeLong(annotationDimension);
    out.writeLong(shapeFlags);

    // Write x,y for all parts
    long previousX = Math.round(xOffset * xyScale);
    long previousY = Math.round(yOffset * xyScale);
    for (final Iterator<CoordinatesList> iterator = pointsList.iterator(); iterator.hasNext();) {
      final CoordinatesList points = iterator.next();
      final int numPoints = points.size();
      for (int i = 0; i < numPoints; i++) {
        previousX = writeOrdinate(out, points, previousX, xyScale, i, 0);
        previousY = writeOrdinate(out, points, previousY, xyScale, i, 1);
      }
      if (iterator.hasNext()) {
        previousX = writeOrdinate(out, previousX, xyScale, -1);
        previousY = writeOrdinate(out, previousY, xyScale, 0);
      }
    }

    // Write z for all parts
    if (hasZ) {
      writeCoordinates(out, pointsList, 2, zOffset, zScale);
    }

    // Write m for all parts
    if (hasM) {
      writeCoordinates(out, pointsList, 3, mOffset, mScale);
    }
    return out.toByteArray();
  }

  public static List<CoordinatesList> getPointsList(final Geometry geometry) {
    final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(geometry);
    if (geometry instanceof Polygon) {
      for (int i = 0; i < pointsList.size(); i++) {
        CoordinatesList points = pointsList.get(0);
        final boolean reverse = false;
        if (i == 0) {
          if (!JtsGeometryUtil.isCCW(points)) {
            points = points.reverse();
          }
        } else if (JtsGeometryUtil.isCCW(points)) {
          points = points.reverse();
        }
        if (reverse) {
          points = points.reverse();
          pointsList.set(i, points);
        }
      }
    } else if (geometry instanceof MultiPoint) {
      throw new IllegalArgumentException("MultiPoint not supported");
    } else if (geometry instanceof MultiPoint) {
      throw new IllegalArgumentException("MultiLineString not supported");
    } else if (geometry instanceof MultiPolygon) {
      throw new IllegalArgumentException("MultiPolygon not supported");
    } else if (geometry instanceof GeometryCollection) {
      throw new IllegalArgumentException("GeometryCollection not supported");
    }
    return pointsList;
  }

  public static double readOordinate(
    final PackedIntegerInputStream in,
    final CoordinatesList points,
    final int index,
    final int axisIndex,
    final double previousValue,
    final double scale) throws IOException {
    final double value = readOordinate(in, previousValue, scale);
    points.setValue(index, axisIndex, value);
    return value;
  }

  public static double readOordinate(
    final PackedIntegerInputStream in,
    final double previousValue,
    final double scale) throws IOException {
    final long deltaValueLong = in.readLong();
    final double deltaValue = deltaValueLong / scale;
    final double value = Math.round((previousValue + deltaValue) * scale)
      / scale;
    return value;
  }

  public static void readOordinates(
    final PackedIntegerInputStream in,
    final List<CoordinatesList> pointsList,
    final int axisIndex,
    final double offset,
    final double scale) throws IOException {

    double previousValue = offset;
    for (final Iterator<CoordinatesList> iterator = pointsList.iterator(); iterator.hasNext();) {
      final CoordinatesList points = iterator.next();
      final int numPoints = points.size();
      for (int i = 1; i < numPoints; i++) {
        previousValue = readOordinate(in, points, i, axisIndex, previousValue,
          scale);
      }
      if (iterator.hasNext()) {
        previousValue = readOordinate(in, previousValue, scale);
      }
    }
  }

  public static long writeCoordinates(
    final PackedIntegerOutputStream out,
    final CoordinatesList points,
    long previousValue,
    final double scale,
    final int ordinateIndex) {
    for (int i = 0; i < points.size(); i++) {
      previousValue = writeOrdinate(out, points, previousValue, scale, i,
        ordinateIndex);
    }
    return previousValue;
  }

  public static void writeCoordinates(
    final PackedIntegerOutputStream out,
    final List<CoordinatesList> pointsList,
    final int axisIndex,
    final double offset,
    final double scale) {
    long previous = Math.round(offset * scale);
    for (final Iterator<CoordinatesList> iterator = pointsList.iterator(); iterator.hasNext();) {
      final CoordinatesList points = iterator.next();
      if (points.getNumAxis() > axisIndex) {
        previous = writeCoordinates(out, points, previous, scale, axisIndex);
      } else {
        previous = writeZeroCoordinates(out, points.size(), scale, previous);
      }
      if (iterator.hasNext()) {
        previous = writeOrdinate(out, previous, scale, 0);
      }
    }
  }

  /**
   * Write the value of an ordinate from the coordinates which has the specified
   * coordinateIndex and ordinateIndex. The value written is the difference
   * between the current value and the previous value which are both multiplied
   * by the scale and rounded to longs before conversion.
   * 
   * @param out The stream to write the bytes to.
   * @param coordinates The coordinates.
   * @param previousValue The value of the previous coordinate, returned from
   *          this method.
   * @param scale The scale which defines the precision of the values.
   * @param coordinateIndex The coordinate index.
   * @param ordinateIndex The ordinate index.
   * @return The current ordinate value * scale rounded to a long value.
   */
  public static long writeOrdinate(
    final PackedIntegerOutputStream out,
    final CoordinatesList coordinates,
    final long previousValue,
    final double scale,
    final int coordinateIndex,
    final int ordinateIndex) {
    final double value = coordinates.getOrdinate(coordinateIndex, ordinateIndex);
    return writeOrdinate(out, previousValue, scale, value);
  }

  public static long writeOrdinate(
    final PackedIntegerOutputStream out,
    final long previousValue,
    final double scale,
    final double value) {
    long longValue;
    if (Double.isNaN(value)) {
      longValue = 0;
    } else {
      longValue = Math.round(value * scale);
    }
    out.writeLong(longValue - previousValue);
    return longValue;
  }

  public static long writeZeroCoordinates(
    final PackedIntegerOutputStream out,
    final int numCoordinates,
    final double scale,
    long previousValue) {
    for (int i = 0; i < numCoordinates; i++) {
      previousValue = writeOrdinate(out, previousValue, scale, 0);
    }
    return previousValue;
  }
}
