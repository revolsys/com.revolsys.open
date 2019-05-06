package com.revolsys.swing.map.layer.ogc.wms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.parallel.Invoke;
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
  public void render(final ViewRenderer view, final OgcWmsImageLayer layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final BoundingBox viewportBoundingBox = view.getBoundingBox();
        final BoundingBox queryBoundingBox = viewportBoundingBox
          .bboxIntersection(layer.getWmsLayerDefinition().getLatLonBoundingBox());

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
          final int imageWidth = (int)Math.ceil(view.getViewWidthPixels());
          final int imageHeight = (int)Math.ceil(view.getViewHeightPixels());
          final OgcWmsImageLayerSwingWorker worker = new OgcWmsImageLayerSwingWorker(this, view,
            queryBoundingBox, imageWidth, imageHeight);
          synchronized (this) {
            if (this.worker != null) {
              this.worker.cancel(true);
            }
            this.worker = worker;
          }
          Invoke.worker(worker);

        } else {
          view.drawImage(this.image, false);
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
