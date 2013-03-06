package com.revolsys.swing.map.table;

import java.awt.Component;

import com.revolsys.swing.map.layer.Layer;

public interface LayerTablePanelFactory {
  Class<? extends Layer> getLayerClass();

  Component createPanel(Layer layer);
}
