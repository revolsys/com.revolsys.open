package com.revolsys.swing.map.layer.raster;

import java.awt.RenderingHints;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class GeoreferencedImageLayerRenderer
  extends AbstractLayerRenderer<GeoreferencedImageLayer> {

  public GeoreferencedImageLayerRenderer(final GeoreferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final ViewRenderer view, final GeoreferencedImageLayer layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GeoreferencedImage image = layer.getImage();
        if (image != null) {
          BoundingBox boundingBox = layer.getBoundingBox();
          if (boundingBox == null || boundingBox.isEmpty()) {
            boundingBox = layer.fitToViewport();
          }
          if (!view.isCancelled()) {
            final double alpha = layer.getOpacity() / 255.0;
            view.drawImage(image, false, alpha, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            view.drawDifferentCoordinateSystem(boundingBox);
          }
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
