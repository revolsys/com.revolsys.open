package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapefileConstants;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class MultiPoint2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public MultiPoint2DConverter() {
    this(null);
  }

  public MultiPoint2DConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory();
    }
  }

  public int getShapeType() {
    return ShapefileConstants.MULTI_POINT_SHAPE;
  }

  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    return ShapefileGeometryUtil.readMultiPoint(geometryFactory, in);
  }

  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {

  }
}
