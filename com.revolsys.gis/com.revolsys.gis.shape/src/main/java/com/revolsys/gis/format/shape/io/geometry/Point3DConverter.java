package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapefileConstants;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Point3DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Point3DConverter() {
    this(null);
  }

  public Point3DConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory();
    }
  }

  public int getShapeType() {
    return ShapefileConstants.POINT_Z_SHAPE;
  }

  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    byte numAxis = 3;
    if (recordLength == 18) {
      numAxis = 4;
    }
    final double[] coordinates = new double[numAxis];
    for (int i = 0; i < numAxis; i++) {
      coordinates[i] = in.readLEDouble();
    }
    final CoordinatesList points = new DoubleCoordinatesList(numAxis,
      coordinates);
    return geometryFactory.createPoint(points);
  }

  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final Coordinate coordinate = point.getCoordinate();
      final int recordLength = 18;
      // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
      out.writeInt(recordLength);
      out.writeLEInt(getShapeType());
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
      if (Double.isNaN(coordinate.z)) {
        out.writeLEDouble(0);
      } else {
        out.writeLEDouble(coordinate.z);
      }
      out.writeLEDouble(0);
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }
}
