package com.revolsys.swing.map.layer.ogc.wms;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

public class OgcWmsImageLayerRenderer extends AbstractLayerRenderer<OgcWmsImageLayer>
  implements PropertyChangeListener {

  public static final String TILES_LOADED = "loading";

  private GeoreferencedImage image;

  private OgcWmsImageLayerSwingWorker worker;

  public OgcWmsImageLayerRenderer(final OgcWmsImageLayer layer) {
    super("ogcWmsLayerRenderer", layer);
    Property.addListener(layer, this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {

  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final OgcWmsImageLayer layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final BoundingBox viewportBoundingBox = viewport.getBoundingBox();
        final BoundingBox queryBoundingBox = viewportBoundingBox
          .intersection(layer.getWmsLayerDefinition().getLatLonBoundingBox());

        boolean reload = false;
        if (this.image == null) {
          reload = true;
        } else if (!queryBoundingBox.equals(this.image.getBoundingBox())) {
          reload = true;
        }
        if (reload) {
          if (this.worker != null) {
            if (!this.worker.equalsBoundingBox(queryBoundingBox)) {
              reload = false;
            }
          }
        }
        if (reload) {
          final int imageWidth = viewport.getViewWidthPixels();
          final int imageHeight = viewport.getViewHeightPixels();
          final OgcWmsImageLayerSwingWorker worker = new OgcWmsImageLayerSwingWorker(this,
            cancellable, queryBoundingBox, imageWidth, imageHeight);
          synchronized (this) {
            if (this.worker != null) {
              this.worker.cancel(true);
            }
            this.worker = worker;
          }
          Invoke.worker(worker);

        } else if (this.image != null) {
          final Graphics2D graphics = viewport.getGraphics();
          if (graphics != null) {
            final GeoreferencedImage image1 = this.image;
            viewport.drawImage(image1, false);
          }
        }
      }
    }
  }

  protected void setImage(final OgcWmsImageLayerSwingWorker worker,
    final GeoreferencedImage image) {
    synchronized (this) {
      if (worker == this.worker) {
        this.worker = null;
      } else {
        return;
      }
    }
    this.image = image;
    final OgcWmsImageLayer layer = getLayer();
    if (layer != null) {
      layer.firePropertyChange("imageLoaded", false, true);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
