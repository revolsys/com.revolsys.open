package com.revolsys.io.shp.geometry;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.vividsolutions.jts.geom.Geometry;

public class Polygon2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Polygon2DConverter() {
    this(null);
  }

  public Polygon2DConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  @Override
  public int getShapeType() {
    return ShapefileConstants.POLYGON_SHAPE;
  }

  @Override
  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    return ShapefileGeometryUtil.SHP_INSTANCE.readPolygon(geometryFactory, in);
  }

  @Override
  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {
    ShapefileGeometryUtil.SHP_INSTANCE.writePolygon(out, geometry);
  }
}
