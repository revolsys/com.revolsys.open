package com.revolsys.swing.map.layer.ogc.wms;

import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class OgcWmsImageLayerRenderer extends AbstractLayerRenderer<OgcWmsImageLayer>
  implements PropertyChangeListener {

  public static final String TILES_LOADED = "loading";

  public OgcWmsImageLayerRenderer(final OgcWmsImageLayer layer) {
    super("ogcWmsLayerRenderer", layer);
  }

  @Override
  public void render(final ViewRenderer view, final OgcWmsImageLayer layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final BoundingBox viewportBoundingBox = view.getBoundingBox();
        final BoundingBox queryBoundingBox = viewportBoundingBox
          .bboxIntersection(layer.getWmsLayerDefinition().getLatLonBoundingBox());

        final Supplier<GeoreferencedImage> constructor = () -> {
          try {
            final WmsLayerDefinition wmsLayerDefinition = layer.getWmsLayerDefinition();
            if (wmsLayerDefinition != null) {
              if (!view.isCancelled()) {
                final int viewWidthPixels = (int)Math.ceil(view.getViewWidthPixels());
                final int viewHeightPixels = (int)Math.ceil(view.getViewHeightPixels());
                return wmsLayerDefinition.getMapImage(queryBoundingBox, viewWidthPixels,
                  viewHeightPixels);
              }
            }
          } catch (final Throwable t) {
            if (!view.isCancelled()) {
              Logs.error(this, "Unable to paint", t);
            }
          }
          return null;
        };
        final GeoreferencedImage image = view.getCachedItemBackground("Refresh " + layer.getPath(),
          layer, "image", constructor);
        if (image != null) {
          view.drawImage(image, false);
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
