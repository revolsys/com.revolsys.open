package com.revolsys.swing.map.layer.raster;

import java.util.function.Function;

import org.jeometry.common.logging.Logs;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class ViewFunctionImageLayerRenderer<L extends AbstractLayer>
  extends AbstractLayerRenderer<L> {

  private final Function<ViewRenderer, GeoreferencedImage> newImageFunction;

  private boolean hasError = false;

  public ViewFunctionImageLayerRenderer(final L layer,
    final Function<ViewRenderer, GeoreferencedImage> newImageFunction) {
    super(layer.getType() + "Renderer", layer.getType() + " Renderer", null);
    setLayer(layer);
    this.newImageFunction = newImageFunction;
    layer.addPropertyChangeListener("refresh", e -> this.hasError = false);
  }

  @Override
  public void render(final ViewRenderer view, final L layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      final String taskName = "Refresh Image " + layer.getPath();
      final GeoreferencedImage image = view.getCachedItemBackground(taskName, layer, "image",
        () -> this.newImageFunction.apply(view), e -> {
          if (!this.hasError) {
            this.hasError = true;
            Logs.error(this, "Error loading '" + layer.getPath()
              + "', move the map or Refresh the layer to try again", e);
          }
        });
      if (image != null) {
        this.hasError = false;
        final GeoreferencedImage projectedImage = image.imageToCs(view, view);
        if (!view.isCancelled()) {
          view.drawImage(projectedImage, false);
        }
      }
    }
  }

  @Override
  public JsonObject toMap() {
    return new JsonObjectHash();
  }
}
