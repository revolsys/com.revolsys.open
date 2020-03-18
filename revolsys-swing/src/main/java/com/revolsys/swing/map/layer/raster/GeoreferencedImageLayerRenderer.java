package com.revolsys.swing.map.layer.raster;

import java.awt.RenderingHints;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class GeoreferencedImageLayerRenderer
  extends AbstractLayerRenderer<GeoreferencedImageLayer> {

  public GeoreferencedImageLayerRenderer(final GeoreferencedImageLayer layer) {
    super("raster", "Raster", null);
    setLayer(layer);
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
            final boolean useTransform = !layer.isShowOriginalImage();
            view.drawImage(image, useTransform, alpha, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            view.drawDifferentCoordinateSystem(boundingBox);
          }
        }
      }
    }
  }

  @Override
  public JsonObject toMap() {
    return JsonObject.EMPTY;
  }
}
