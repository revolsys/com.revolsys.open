package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.io.EndianInput;
import com.vividsolutions.jts.geom.Geometry;

public interface ShapefileGeometryReader {

  Geometry read(EndianInput in, long recordLength) throws IOException;
}
