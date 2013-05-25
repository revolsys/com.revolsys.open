package com.revolsys.swing.map.overlay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;

public class LayerRendererOverlaySwingWorker extends SwingWorker<Void, Void> {

  private final LayerRendererOverlay overlay;

  private final GeoReferencedImage referencedImage;

  public LayerRendererOverlaySwingWorker(
    final LayerRendererOverlay layerRendererOverlay,
    final GeoReferencedImage image) {
    this.overlay = layerRendererOverlay;
    this.referencedImage = image;
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      final Layer layer = overlay.getLayer();
      if (layer != null) {
        final Project project = overlay.getProject();
        final int imageWidth = referencedImage.getImageWidth();
        final int imageHeight = referencedImage.getImageHeight();
        if (imageWidth > 0 && imageHeight > 0 && project != null) {
          final BoundingBox boundingBox = referencedImage.getBoundingBox();
          final ImageViewport viewport = new ImageViewport(project, imageWidth,
            imageHeight, boundingBox);

          final Graphics2D graphics = viewport.getGraphics();
          if (layer != null && layer.isVisible()) {
            final LayerRenderer<Layer> renderer = layer.getRenderer();
            if (renderer != null) {
              renderer.render(viewport, graphics);
            }
          }
          graphics.dispose();
          final BufferedImage image = viewport.getImage();
          this.referencedImage.setImage(image);
        }
      }
      return null;
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to paint", t);
      return null;
    }
  }

  @Override
  protected void done() {
    overlay.setImage(this);
  }

  public GeoReferencedImage getReferencedImage() {
    return referencedImage;
  }
}
