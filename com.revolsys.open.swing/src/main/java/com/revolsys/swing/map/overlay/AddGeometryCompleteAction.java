package com.revolsys.swing.map.overlay;

import com.revolsys.geometry.model.Geometry;

public interface AddGeometryCompleteAction {
  void addComplete(AbstractOverlay overlay, Geometry geometry);
}
