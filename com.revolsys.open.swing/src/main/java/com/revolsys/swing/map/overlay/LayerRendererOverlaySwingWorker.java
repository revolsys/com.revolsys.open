package com.revolsys.swing.map.overlay;

import java.awt.image.BufferedImage;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.util.Cancellable;

public class LayerRendererOverlaySwingWorker extends AbstractSwingWorker<Void, Void>
  implements Cancellable {

  private final LayerRendererOverlay overlay;

  private final GeoreferencedImage referencedImage;

  public LayerRendererOverlaySwingWorker(final LayerRendererOverlay layerRendererOverlay,
    final GeoreferencedImage image) {
    this.overlay = layerRendererOverlay;
    this.referencedImage = image;
  }

  public GeoreferencedImage getReferencedImage() {
    return this.referencedImage;
  }

  @Override
  protected Void handleBackground() {
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
                renderer.render(viewport, this);
              }
            }
            if (!isCancelled()) {
              final BufferedImage image = viewport.getImage();
              this.referencedImage.setRenderedImage(image);
            }
          }
        }
      }
      return null;
    } catch (final Throwable t) {
      if (!isCancelled()) {
        Logs.error(this, "Unable to paint", t);
      }
      return null;
    }
  }

  @Override
  protected void handleDone(final Void result) {
    this.overlay.setImage(this);
  }

  @Override
  public String toString() {
    return "Render layers";
  }
}
