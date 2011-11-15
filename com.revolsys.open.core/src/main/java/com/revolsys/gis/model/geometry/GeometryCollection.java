package com.revolsys.gis.model.geometry;

import java.util.List;

public interface GeometryCollection<T extends Geometry> extends Geometry,
  List<T> {

}
