package com.revolsys.swing.map.overlay;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;

public class LayerRendererOverlaySwingWorker extends SwingWorker<Void, Void> {

  private LayerRendererOverlay overlay;

  private GeoReferencedImage referencedImage;

  public LayerRendererOverlaySwingWorker(
    LayerRendererOverlay layerRendererOverlay, GeoReferencedImage image) {
    this.overlay = layerRendererOverlay;
    this.referencedImage = image;
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      Layer layer = overlay.getLayer();
      if (layer != null) {
        Project project = overlay.getProject();
        int imageWidth = referencedImage.getImageWidth();
        int imageHeight = referencedImage.getImageHeight();
        if (imageWidth > 0 && imageHeight > 0 && project != null) {
          BoundingBox boundingBox = referencedImage.getBoundingBox();
          ImageViewport viewport = new ImageViewport(project, imageWidth,
            imageHeight, boundingBox);

          Graphics2D graphics = viewport.getGraphics();
          if (layer != null && layer.isVisible()) {
            LayerRenderer<Layer> renderer = layer.getRenderer();
            if (renderer != null) {
              renderer.render(viewport, graphics);
            }
          }
          graphics.dispose();
          Image image = viewport.getImage();
          this.referencedImage.setImage(image);
        }
      }
      return null;
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to paint", t);
      return null;
    }
  }

  public GeoReferencedImage getReferencedImage() {
    return referencedImage;
  }

  @Override
  protected void done() {
    overlay.setImage(this);
  }
}
