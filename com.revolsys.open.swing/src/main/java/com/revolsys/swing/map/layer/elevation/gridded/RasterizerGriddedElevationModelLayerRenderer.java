package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelImage;
import com.revolsys.elevation.gridded.rasterizer.ColourGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayerRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.Cancellable;

public class RasterizerGriddedElevationModelLayerRenderer
  extends AbstractGriddedElevationModelLayerRenderer {

  static {
    MenuFactory.addMenuInitializer(RasterizerGriddedElevationModelLayerRenderer.class,
      menu -> Menus.addMenuItem(menu, "layer", "Delete", "delete",
        RasterizerGriddedElevationModelLayerRenderer::isHasParent,
        RasterizerGriddedElevationModelLayerRenderer::delete, true));
  }

  private GriddedElevationModelImage image;

  private Thread worker;

  private GriddedElevationModelRasterizer rasterizer;

  private boolean redraw = true;

  private float opacity = 1;

  private RasterizerGriddedElevationModelLayerRenderer() {
    super("rasterizerGriddedElevationModelLayerRenderer", "DEM Style");
  }

  public RasterizerGriddedElevationModelLayerRenderer(final IGriddedElevationModelLayer layer,
    final MultipleLayerRenderer<IGriddedElevationModelLayer, RasterizerGriddedElevationModelLayerRenderer> parent) {
    this();
    setLayer(layer);
    setParent((LayerRenderer<?>)parent);
  }

  public RasterizerGriddedElevationModelLayerRenderer(final IGriddedElevationModelLayer layer,
    final MultipleLayerRenderer<IGriddedElevationModelLayer, RasterizerGriddedElevationModelLayerRenderer> parent,
    final GriddedElevationModelRasterizer rasterizer) {
    this();
    setLayer(layer);
    setParent((LayerRenderer<?>)parent);
    setRasterizer(rasterizer);
    if (rasterizer != null) {
      final String name = rasterizer.getName();
      setName(name);
    }
  }

  public RasterizerGriddedElevationModelLayerRenderer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @SuppressWarnings("unchecked")
  public void delete() {
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof MultipleLayerRenderer) {
      final MultipleLayerRenderer<IGriddedElevationModelLayer, LayerRenderer<IGriddedElevationModelLayer>> multiple = (MultipleLayerRenderer<IGriddedElevationModelLayer, LayerRenderer<IGriddedElevationModelLayer>>)parent;
      multiple.removeRenderer(this);
    }
  }

  public float getOpacity() {
    return this.opacity;
  }

  public GriddedElevationModelRasterizer getRasterizer() {
    return this.rasterizer;
  }

  @Override
  public Form newStylePanel() {
    return new GriddedElevationModelStylePanel(this);
  }

  @Override
  public void refresh() {
    this.redraw = true;
  }

  @Override
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final IGriddedElevationModelLayer layer) {
    // TODO cancel
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GriddedElevationModel elevationModel = getElevationModel();
        if (elevationModel != null) {
          synchronized (this) {
            if (this.rasterizer == null) {
              final ColourGriddedElevationModelRasterizer rasterizer = new ColourGriddedElevationModelRasterizer();
              setRasterizer(rasterizer);

              final String name = this.rasterizer.getName();
              setName(name);
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

          if (this.image.hasImage() && !(this.image.isCached() && this.redraw)) {
            final BoundingBox boundingBox = layer.getBoundingBox();
            final Graphics2D graphics = viewport.getGraphics();
            if (graphics != null) {
              final Object interpolationMethod = null;
              GeoreferencedImageLayerRenderer.renderAlpha(viewport, graphics, this.image, true,
                this.opacity, interpolationMethod);
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
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
    if (this.rasterizer != null) {
      this.rasterizer.setElevationModel(elevationModel);
    }
  }

  public void setOpacity(final float opacity) {
    final float oldValue = this.opacity;
    if (opacity < 0) {
      this.opacity = 0;
    } else if (opacity > 1) {
      this.opacity = 1;
    } else {
      this.opacity = opacity;
    }
    firePropertyChange("opacity", oldValue, opacity);
  }

  public void setRasterizer(final GriddedElevationModelRasterizer rasterizer) {
    this.rasterizer = rasterizer;
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof GriddedElevationModelZRange) {
      final GriddedElevationModelZRange zRange = (GriddedElevationModelZRange)parent;
      if (!Double.isFinite(rasterizer.getMinZ())) {
        final double minZ = zRange.getMinZ();
        rasterizer.setMinZ(minZ);
      }
      if (!Double.isFinite(rasterizer.getMaxZ())) {
        final double maxZ = zRange.getMaxZ();
        rasterizer.setMaxZ(maxZ);
      }
    }
    final GriddedElevationModel elevationModel = getElevationModel();
    if (elevationModel != null) {
      rasterizer.setElevationModel(elevationModel);
    }
    this.redraw = true;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "rasterizer", this.rasterizer);
    addToMap(map, "opacity", this.opacity);
    return map;
  }
}
