package com.revolsys.swing.map.overlay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;

/**
 * <p>A lightweight component that users the {@link Layer}'s {@link LayerRenderer} to render the layer.</p>
 */
@SuppressWarnings("serial")
public class LayerRendererOverlay extends JComponent implements
  PropertyChangeListener {

  private Layer layer;

  private Viewport2D viewport;

  public LayerRendererOverlay(final Viewport2D viewport, final Layer layer) {
    this.viewport = viewport;
    setLayer(layer);
  }

  public LayerRendererOverlay(Viewport2D viewport) {
    this(viewport, null);
  }

  public void dispose() {
    if (layer != null) {
      layer.removePropertyChangeListener(this);
      layer = null;
    }
    viewport = null;
  }

  public Layer getLayer() {
    return layer;
  }

  @Override
  public void paintComponent(final Graphics g) {
    try {
      Layer layer = getLayer();
      if (layer != null && layer.isVisible()) {
        Graphics2D graphics2d = (Graphics2D)g;
        LayerRenderer<Layer> renderer = layer.getRenderer();
        if (renderer != null) {
          renderer.render(viewport, graphics2d);
        }
      }
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to paint", t);
    }
  }

  public void setLayer(Layer layer) {
    if (this.layer != layer) {
      if (this.layer != null) {
        layer.removePropertyChangeListener(this);
      }
      this.layer = layer;
      if (layer != null) {
        layer.addPropertyChangeListener(this);
      }
      repaint();
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    repaint();
  }
}
