package com.revolsys.jump.ui.model;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.RendererFactory;

public class GridRendererFactory implements RendererFactory<GridLayer> {

  public Renderer create(final GridLayer layer,
    final LayerViewPanel layerViewPanel, final int maxFeatures) {
    return new GridRenderer(layer, layerViewPanel);
  }

}
