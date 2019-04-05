package com.revolsys.swing.map.layer.ogc.wms;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.util.Cancellable;

public class OgcWmsImageLayerSwingWorker extends AbstractSwingWorker<GeoreferencedImage, Void> {

  private final OgcWmsImageLayerRenderer renderer;

  private GeoreferencedImage image;

  private final BoundingBox boundingBox;

  private final int imageWidth;

  private final int imageHeight;

  private final Cancellable cancellable;

  public OgcWmsImageLayerSwingWorker(final OgcWmsImageLayerRenderer renderer,
    final Cancellable cancellable, final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    this.renderer = renderer;
    this.cancellable = cancellable;
    this.boundingBox = boundingBox;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public boolean equalsBoundingBox(final BoundingBox boundingBox) {
    return this.boundingBox.equals(boundingBox);
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public GeoreferencedImage getReferencedImage() {
    return this.image;
  }

  @Override
  protected GeoreferencedImage handleBackground() {
    try {
      final OgcWmsImageLayer layer = this.renderer.getLayer();
      if (layer != null) {
        final WmsLayerDefinition wmsLayerDefinition = layer.getWmsLayerDefinition();
        if (wmsLayerDefinition != null) {
          if (!this.cancellable.isCancelled()) {
            return wmsLayerDefinition.getMapImage(this.boundingBox, this.imageWidth,
              this.imageHeight);
          }
        }
      }
    } catch (final Throwable t) {
      if (!isCancelled() && !this.cancellable.isCancelled()) {
        Logs.error(this, "Unable to paint", t);
      }
    }
    return null;
  }

  @Override
  protected void handleCancelled() {
    this.renderer.setImage(this, null);
  }

  @Override
  protected void handleDone(final GeoreferencedImage image) {
    if (!this.cancellable.isCancelled()) {
      this.renderer.setImage(this, image);
    }
  }

  @Override
  public String toString() {
    return "OGC WMS GetMap";
  }
}
