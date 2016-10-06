package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Graphics2D;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;

public class GriddedElevationModelLayerRenderer
  extends AbstractLayerRenderer<GriddedElevationModelLayer> {

  public GriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final GriddedElevationModelLayer layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GriddedElevationModel elevationModel = layer.getElevationModel();
        if (elevationModel != null) {
          final GeoreferencedImage image = elevationModel.getImage();
          if (image != null) {
            final BoundingBox boundingBox = layer.getBoundingBox();
            final Graphics2D graphics = viewport.getGraphics();
            if (graphics != null) {
              GeoreferencedImageLayerRenderer.renderAlpha(viewport, graphics, image, true,
                layer.getOpacity() / 255.0);
              GeoreferencedImageLayerRenderer.renderDifferentCoordinateSystem(viewport, graphics,
                boundingBox);
            }
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
