package com.revolsys.swing.map.overlay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;

public class LayerRendererOverlaySwingWorker extends SwingWorker<Void, Void> {

  private LayerRendererOverlay overlay;

  private BufferedImage image;

  public LayerRendererOverlaySwingWorker(
    LayerRendererOverlay layerRendererOverlay) {
    this.overlay = layerRendererOverlay;
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      Viewport2D viewport = overlay.getViewport();
      int width = viewport.getViewWidthPixels();
      int height = viewport.getViewHeightPixels();
      BufferedImage image = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);

      JComponent parent = (JComponent)overlay.getParent();
      Graphics2D graphics = (Graphics2D)image.getGraphics();
      Insets insets = parent.getInsets();
//      graphics.translate(-insets.left, -insets.top);
      Layer layer = overlay.getLayer();
      if (layer != null && layer.isVisible()) {
        LayerRenderer<Layer> renderer = layer.getRenderer();
        if (renderer != null) {
          renderer.render(viewport, graphics);
        }
      }
      graphics.dispose();
      this.image = image;

      return null;
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to paint", t);
      return null;
    }
  }

  public BufferedImage getImage() {
    return image;
  }

  @Override
  protected void done() {
    overlay.setImage(this);
  }
}
