package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PackedCoordinateUtil {
  public static CoordinateSequence getCoordinateSequence(
    final int numPoints,
    final Double xOffset,
    final Double yOffset,
    final Double xyScale,
    final Double zOffset,
    final Double zScale,
    final Double mOffset,
    final Double mScale,
    final byte[] packedCoordinates) {
    final InputStream ins = new ByteArrayInputStream(packedCoordinates);
    final PackedIntegerInputStream in = new PackedIntegerInputStream(ins);

    try {
      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      double[] oordinates;
      int dimension;
      if (hasM) {
        dimension = 4;
      } else if (hasZ) {
        dimension = 3;
      } else {
        dimension = 2;
      }
      oordinates = new double[numPoints * dimension];

      double x = readOordinate(in, oordinates, dimension, 0, 0, xOffset,
        xyScale);
      double y = readOordinate(in, oordinates, dimension, 0, 1, yOffset,
        xyScale);

      for (int i = 1; i < numPoints; i++) {
        x = readOordinate(in, oordinates, dimension, i, 0, x, xyScale);
        y = readOordinate(in, oordinates, dimension, i, 1, y, xyScale);
        // TODO x=-1, y = 0 part separator
      }

      if (hasZ) {
        readCoordinates(in, oordinates, numPoints, dimension, 2, zOffset,
          zScale);
      }
      if (hasM) {
        readCoordinates(in, oordinates, numPoints, dimension, 3, mOffset,
          mScale);
      }

      final CoordinateSequence coordinates = new DoubleCoordinatesList(
        oordinates, dimension);
      return coordinates;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(ins);
    }
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
    final CoordinateSequence coordinates) {
    final int numCoordinates = coordinates.size();
    final int dimension = coordinates.getDimension();

    final int packedByteLength = 0;
    byte dimensionFlag = 0;
    final byte annotationDimension = 0;
    final byte shapeFlags = 0;

    if (hasZ) {
      dimensionFlag |= 1;
    }
    if (hasM) {
      dimensionFlag |= 2;
    }

    final PackedIntegerOutputStream out = new PackedIntegerOutputStream(
      numCoordinates, dimension);
    out.writeLong5(packedByteLength);
    out.writeLong(dimensionFlag);
    out.writeLong(annotationDimension);
    out.writeLong(shapeFlags);

    long previousX = writeOrdinate(out, coordinates, Math.round(xOffset
      * xyScale), xyScale, 0, 0);
    long previousY = writeOrdinate(out, coordinates, Math.round(yOffset
      * xyScale), xyScale, 0, 1);

    for (int i = 1; i < numCoordinates; i++) {
      previousX = writeOrdinate(out, coordinates, previousX, xyScale, i, 0);
      previousY = writeOrdinate(out, coordinates, previousY, xyScale, i, 1);
    }
    if (hasZ) {
      if (dimension > 2) {
        writeCoordinates(out, coordinates, numCoordinates, Math.round(zOffset
          * zScale), zScale, 2);
      } else {
        writeZeroCoordinates(out, numCoordinates, Math.round(zOffset * zScale));
      }
    }
    if (hasM) {
      if (dimension > 3) {
        writeCoordinates(out, coordinates, numCoordinates, Math.round(mOffset
          * mScale), mScale, 2);
      } else {
        writeZeroCoordinates(out, numCoordinates, Math.round(mOffset * mScale));
      }
    }
    return out.toByteArray();
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
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return getPackedBytes(xOffset, yOffset, xyScale, hasZ, zOffset, zScale,
        hasM, mOffset, mScale, point.getCoordinateSequence());
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return getPackedBytes(xOffset, yOffset, xyScale, hasZ, zOffset, zScale,
        hasM, mOffset, mScale, line.getCoordinateSequence());
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      if (polygon.getNumInteriorRing() > 0) {
        throw new IllegalArgumentException(
          "Polygons with holes are not supported");
      }
      return getPackedBytes(xOffset, yOffset, xyScale, hasZ, zOffset, zScale,
        hasM, mOffset, mScale, polygon.getExteriorRing()
          .getCoordinateSequence());
    } else {
      throw new IllegalArgumentException("Geometry type not supported");
    }
  }

  private static void readCoordinates(
    final PackedIntegerInputStream in,
    final double[] oordinates,
    final int numPoints,
    final int dimension,
    final int ordinateIndex,
    final Double offset,
    final Double scale)
    throws IOException {
    double z = readOordinate(in, oordinates, dimension, 0, ordinateIndex,
      offset, scale);
    for (int i = 1; i < numPoints; i++) {
      z = readOordinate(in, oordinates, dimension, i, ordinateIndex, z, scale);
      // TODO part separator
    }
  }

  public static double readOordinate(
    final PackedIntegerInputStream in,
    final double[] oordinates,
    final int dimension,
    final int coordinateIndex,
    final int ordinateIndex,
    final double previousValue,
    final double scale)
    throws IOException {
    final long deltaValueLong = in.readLong();
    final double deltaValue = deltaValueLong / scale;
    final double value = Math.round((previousValue + deltaValue) * scale)
      / scale;
    oordinates[coordinateIndex * dimension + ordinateIndex] = value;
    return value;
  }

  private static void writeCoordinates(
    final PackedIntegerOutputStream out,
    final CoordinateSequence coordinates,
    final int numCoordinates,
    final long offset,
    final double scale,
    final int ordinateIndex) {
    long previousValue = writeOrdinate(out, coordinates, offset, scale, 0,
      ordinateIndex);

    for (int i = 1; i < numCoordinates; i++) {
      previousValue = writeOrdinate(out, coordinates, previousValue, scale, i,
        ordinateIndex);
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
  private static long writeOrdinate(
    final PackedIntegerOutputStream out,
    final CoordinateSequence coordinates,
    final long previousValue,
    final Double scale,
    final int coordinateIndex,
    final int ordinateIndex) {
    final double value = coordinates.getOrdinate(coordinateIndex, ordinateIndex);
    long longValue;
    if (Double.isNaN(value)) {
      longValue = 0;
    } else {
      longValue = Math.round(value * scale);
    }
    out.writeLong(longValue - previousValue);
    return longValue;
  }

  private static void writeZeroCoordinates(
    final PackedIntegerOutputStream out,
    final int numCoordinates,
    final long offset) {
    out.writeLong(offset);
    for (int i = 1; i < numCoordinates; i++) {
      out.writeLong(0);
    }
  }
}
