package com.revolsys.gis.data.io;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryReader extends IteratorReader<Geometry> {
  public GeometryReader(
    Iterator<Geometry> iterator) {
    super(iterator);
  }
}
