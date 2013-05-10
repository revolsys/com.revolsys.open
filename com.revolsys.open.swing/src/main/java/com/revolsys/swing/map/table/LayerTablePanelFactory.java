package com.revolsys.swing.map.table;

import java.awt.Component;

import com.revolsys.swing.map.layer.Layer;

public interface LayerTablePanelFactory {
  Component createPanel(Layer layer);

  Class<? extends Layer> getLayerClass();
}
