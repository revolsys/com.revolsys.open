package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Geometry;

public interface GeometryOperation {
  <T extends Geometry> T perform(T geometry);
}
