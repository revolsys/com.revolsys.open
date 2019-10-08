package com.revolsys.swing.map.layer.raster;

import java.util.function.Function;

import com.revolsys.collection.map.MapEx;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class ViewFunctionImageLayerRenderer<L extends AbstractLayer>
  extends AbstractLayerRenderer<L> {

  private final Function<ViewRenderer, GeoreferencedImage> newImageFunction;

  public ViewFunctionImageLayerRenderer(final L layer,
    final Function<ViewRenderer, GeoreferencedImage> newImageFunction) {
    super(layer.getType() + "Renderer", layer);
    this.newImageFunction = newImageFunction;
  }

  @Override
  public void render(final ViewRenderer view, final L layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      final String taskName = "Refresh Image " + layer.getPath();
      final GeoreferencedImage image = view.getCachedItemBackground(taskName, layer, "image",
        () -> this.newImageFunction.apply(view));
      if (image != null) {
        final GeoreferencedImage projectedImage = image.imageToCs(view, view);
        if (!view.isCancelled()) {
          view.drawImage(projectedImage, false);
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
