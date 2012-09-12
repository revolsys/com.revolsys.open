package com.revolsys.io.shp.geometry;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.vividsolutions.jts.geom.Geometry;

public class MultiPoint2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public MultiPoint2DConverter() {
    this(null);
  }

  public MultiPoint2DConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  @Override
  public int getShapeType() {
    return ShapefileConstants.MULTI_POINT_SHAPE;
  }

  @Override
  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    return ShapefileGeometryUtil.INSTANCE.readMultipoint(geometryFactory, in);
  }

  @Override
  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {

  }
}
