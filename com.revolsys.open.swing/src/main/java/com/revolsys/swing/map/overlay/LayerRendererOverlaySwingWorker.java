package com.revolsys.swing.map.overlay;

import java.awt.image.BufferedImage;

import org.slf4j.LoggerFactory;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class LayerRendererOverlaySwingWorker extends AbstractSwingWorker<Void, Void> {

  private final LayerRendererOverlay overlay;

  private final GeoReferencedImage referencedImage;

  public LayerRendererOverlaySwingWorker(final LayerRendererOverlay layerRendererOverlay,
    final GeoReferencedImage image) {
    this.overlay = layerRendererOverlay;
    this.referencedImage = image;
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      final Layer layer = this.overlay.getLayer();
      if (layer != null) {
        final Project project = this.overlay.getProject();
        final int imageWidth = this.referencedImage.getImageWidth();
        final int imageHeight = this.referencedImage.getImageHeight();
        if (imageWidth > 0 && imageHeight > 0 && project != null) {
          final BoundingBox boundingBox = this.referencedImage.getBoundingBox();
          try (
            final ImageViewport viewport = new ImageViewport(project, imageWidth, imageHeight,
              boundingBox)) {

            if (layer != null && layer.isExists() && layer.isVisible()) {
              final LayerRenderer<Layer> renderer = layer.getRenderer();
              if (renderer != null) {
                renderer.render(viewport);
              }
            }
            final BufferedImage image = viewport.getImage();
            this.referencedImage.setRenderedImage(image);
          }
        }
      }
      return null;
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to paint", t);
      return null;
    }
  }

  public GeoReferencedImage getReferencedImage() {
    return this.referencedImage;
  }

  @Override
  public String toString() {
    return "Render layers";
  }

  @Override
  protected void uiTask() {
    this.overlay.setImage(this);
  }
}
