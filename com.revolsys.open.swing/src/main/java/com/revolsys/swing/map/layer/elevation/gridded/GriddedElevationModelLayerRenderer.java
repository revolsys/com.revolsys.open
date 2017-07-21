package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Graphics2D;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelImage;
import com.revolsys.elevation.gridded.HillShadeConfiguration;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;
import com.revolsys.util.Cancellable;

public class GriddedElevationModelLayerRenderer
  extends AbstractLayerRenderer<GriddedElevationModelLayer> {

  private GriddedElevationModelImage image;

  private Thread worker;

  public GriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer) {
    super("raster", layer);
    final GriddedElevationModel elevationModel = layer.getElevationModel();
    if (elevationModel != null) {
      this.image = new GriddedElevationModelImage(elevationModel);
    }
  }

  public void refresh() {
    synchronized (this) {
      this.worker = null;
      this.image = null;
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final GriddedElevationModelLayer layer) {
    // TODO cancel
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GriddedElevationModel elevationModel = layer.getElevationModel();
        if (elevationModel != null) {
          if (this.image == null) {
            synchronized (this) {
              if (this.worker == null) {
                this.worker = new Thread(() -> {
                  final GriddedElevationModelImage image = new GriddedElevationModelImage(
                    elevationModel);
                  // image.refresh(elevationModel);
                  image.refresh(new HillShadeConfiguration(elevationModel));
                  synchronized (this) {
                    if (this.worker == Thread.currentThread()) {
                      this.image = image;
                      this.worker = null;
                    }
                    layer.redraw();
                  }
                });
                this.worker.start();
              }
            }
          } else {
            final BoundingBox boundingBox = layer.getBoundingBox();
            final Graphics2D graphics = viewport.getGraphics();
            if (graphics != null) {
              GeoreferencedImageLayerRenderer.renderAlpha(viewport, graphics, this.image, true,
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
