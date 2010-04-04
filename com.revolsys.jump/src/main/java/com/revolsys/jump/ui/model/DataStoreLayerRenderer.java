package com.revolsys.jump.ui.model;

import java.awt.Graphics2D;

import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;

public class DataStoreLayerRenderer implements Renderer {

  private DataStoreLayer layer;

  private boolean cancelled;

  private volatile boolean rendering = false;

  private LayerViewPanel layerViewPanel;

  public DataStoreLayerRenderer(final DataStoreLayer layer,
    final LayerViewPanel layerViewPanel) {
    this.layer = layer;
    this.layerViewPanel = layerViewPanel;
  }

  public void cancel() {
    cancelled = true;
  }

  public void clearImageCache() {

  }

  public void copyTo(final Graphics2D graphics) {
    try {
      rendering = true;
      cancelled = false;
      if (render()) {
        
      }
    } catch (Throwable t) {
      t.printStackTrace();
      return;
    } finally {
      rendering = false;
      cancelled = false;
    }
  }

  public boolean render() {
    if (!layer.isVisible()) {
      return false;
    }
    if (!layer.getLayerManager().getLayerables(Layerable.class).contains(layer)) {
      return false;
    }
    return withinVisibleScaleRange();
  }

  public boolean withinVisibleScaleRange() {
    if (layer.isScaleDependentRenderingEnabled()) {
      Double maxScale = layer.getMaxScale();
      Double minScale = layer.getMinScale();
      if (maxScale != null && minScale != null) {
        Viewport viewport = layerViewPanel.getViewport();
        double scale = 1d / viewport.getScale();
        if (scale < layer.getMaxScale()) {
          return false;
        }
        if (scale > layer.getMinScale()) {
          return false;
        }

      }

    }
    return true;
  }

  public Runnable createRunnable() {
    return null;
  }

  public Object getContentID() {
    return layer;
  }

  public boolean isRendering() {
    return rendering;
  }
}
