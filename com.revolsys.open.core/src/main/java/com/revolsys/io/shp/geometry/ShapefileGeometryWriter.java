package com.revolsys.io.shp.geometry;

import java.io.IOException;

import com.revolsys.gis.io.EndianOutput;
import com.vividsolutions.jts.geom.Geometry;

public interface ShapefileGeometryWriter {

  int getShapeType();

  void write(EndianOutput out, Geometry geometry) throws IOException;

}
