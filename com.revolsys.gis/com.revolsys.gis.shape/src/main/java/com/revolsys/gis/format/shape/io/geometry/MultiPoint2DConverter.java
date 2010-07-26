package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapeConstants;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class MultiPoint2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public MultiPoint2DConverter() {
    this(null);
  }

  public MultiPoint2DConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapeConstants.MULTI_POINT_SHAPE;
  }

  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList coordinates = new DoubleCoordinatesList(numPoints,
      2);
    ShapefileGeometryUtil.readCoordinates(in, coordinates);
    return geometryFactory.createMultiPoint(coordinates);
  }

  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {

  }
}
