package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapeConstants;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Point2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Point2DConverter() {
    this(null);
  }

  public Point2DConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapeConstants.POINT_SHAPE;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryConverter#read
   * (int, com.revolsys.gis.format.core.io.LittleEndianRandomAccessFile)
   */
  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double[] ordinates = new double[] {
      x, y
    };
    final CoordinateSequence coordinates = new DoubleCoordinatesList(ordinates,
      2);
    return geometryFactory.createPoint(coordinates);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryConverter#write
   * (com.revolsys.gis.format.core.io.LittleEndianRandomAccessFile,
   * com.vividsolutions.jts.geom.Geometry)
   */
  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final Coordinate coordinate = point.getCoordinate();
      final int recordLength = 10;
      // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
      out.writeInt(recordLength);
      out.writeLEInt(getShapeType());
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }
}
