package com.revolsys.swing.map.overlay;

import com.revolsys.swing.map.layer.Layer;

public interface LayerMapOverlay extends MapOverlay {

  void destroy();

  Layer getLayer();

  void refresh();

  void setLayer(Layer instance);

  void setShowAreaBoundingBox(boolean selected);

}
