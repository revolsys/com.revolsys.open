package com.revolsys.jump.ui.model;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.RendererFactory;

public class DataStoreLayerRendererFactory implements RendererFactory<DataStoreLayer> {

  public Renderer create(final DataStoreLayer layer,
    final LayerViewPanel layerViewPanel, final int maxFeatures) {
    return new DataStoreLayerRenderer(layer, layerViewPanel);
  }

}
