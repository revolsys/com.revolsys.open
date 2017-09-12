package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Graphics2D;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelImage;
import com.revolsys.elevation.gridded.rasterizer.GreyscaleRasterizer;
import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;
import com.revolsys.util.Cancellable;

public class GriddedElevationModelLayerRenderer
  extends AbstractLayerRenderer<GriddedElevationModelLayer> {

  private GriddedElevationModelImage image;

  private Thread worker;

  private GriddedElevationModelRasterizer rasterizer;

  private boolean redraw = true;

  public GriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer) {
    super("raster", layer);
  }

  public void refresh() {
    this.redraw = true;
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
          synchronized (this) {
            if (this.rasterizer == null) {
              // this.rasterizer = new HillShadeConfiguration(elevationModel);
              this.rasterizer = new GreyscaleRasterizer(elevationModel);
            }
            if (this.image == null) {
              this.image = new GriddedElevationModelImage(this.rasterizer);
            }
            if (this.image.getElevationModel() != elevationModel) {
              this.image.setElevationModel(elevationModel);
              this.redraw = true;
            }
            if (this.rasterizer != this.image.getRasterizer()) {
              this.image.setRasterizer(this.rasterizer);
              this.redraw = true;
            }
          }

          if (this.image.hasImage() && !this.redraw) {
            final BoundingBox boundingBox = layer.getBoundingBox();
            final Graphics2D graphics = viewport.getGraphics();
            if (graphics != null) {
              final Object interpolationMethod = null;
              GeoreferencedImageLayerRenderer.renderAlpha(viewport, graphics, this.image, true,
                layer.getOpacity() / 255.0, interpolationMethod);
              GeoreferencedImageLayerRenderer.renderDifferentCoordinateSystem(viewport, graphics,
                boundingBox);
            }
          } else {
            synchronized (this) {
              if (this.redraw && this.worker == null) {
                this.redraw = false;
                this.worker = new Thread(() -> {
                  synchronized (this) {
                    if (this.worker == Thread.currentThread()) {
                      this.image.redraw();
                      this.worker = null;
                    }
                    layer.redraw();
                  }
                });
                this.worker.start();
              }
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
