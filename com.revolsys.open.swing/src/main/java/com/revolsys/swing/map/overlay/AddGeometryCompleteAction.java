package com.revolsys.swing.map.overlay;

import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.vividsolutions.jts.geom.Geometry;

public interface AddGeometryCompleteAction {
  LayerDataObject addComplete(AbstractOverlay overlay, Geometry geometry);
}
