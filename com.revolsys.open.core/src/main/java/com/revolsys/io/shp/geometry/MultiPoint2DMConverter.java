package com.revolsys.io.shp.geometry;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;

public class MultiPoint2DMConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public MultiPoint2DMConverter() {
    this(null);
  }

  public MultiPoint2DMConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  public int getShapeType() {
    return ShapefileConstants.MULTI_POINT_SHAPE;
  }

  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numPoints = in.readLEInt();
    final CoordinatesList coordinates = new DoubleCoordinatesList(numPoints, 4);
    ShapefileGeometryUtil.INSTANCE.readXYCoordinates(in, coordinates);
    ShapefileGeometryUtil.INSTANCE.readCoordinates(in, coordinates, 3);
    return geometryFactory.createMultiPoint(coordinates);
  }

  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {

  }
}
