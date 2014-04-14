package com.revolsys.jtstest.testbuilder.io.shapefile;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;


public interface ShapeHandler {
    public int getShapeType();
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws java.io.IOException,InvalidShapefileException;
    public int getLength(Geometry geometry); //length in 16bit words
}
