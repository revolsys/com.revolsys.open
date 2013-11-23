package com.revolsys.swing.map.overlay;

import com.vividsolutions.jts.geom.Geometry;

public interface AddGeometryCompleteAction {
  void addComplete(AbstractOverlay overlay, Geometry geometry);
}
