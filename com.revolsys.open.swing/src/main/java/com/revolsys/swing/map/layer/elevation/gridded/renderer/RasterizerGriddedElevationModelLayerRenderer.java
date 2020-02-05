package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.BiFunction;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelImage;
import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.SlopeColorGradientGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelStylePanel;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelZRange;
import com.revolsys.swing.map.layer.elevation.gridded.TiledGriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl.GriddedElevationModelImageRasterizer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl.JoclGriddedElevationModelImageRasterizer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;

public class RasterizerGriddedElevationModelLayerRenderer
  extends AbstractGriddedElevationModelLayerRenderer {

  static {
    MenuFactory.addMenuInitializer(RasterizerGriddedElevationModelLayerRenderer.class,
      menu -> menu.addMenuItem("layer", -1, "Delete", "delete",
        RasterizerGriddedElevationModelLayerRenderer::isHasParent,
        RasterizerGriddedElevationModelLayerRenderer::delete, true));
  }

  private static void addAddMenuItem(final MenuFactory menu, final String type,
    final BiFunction<ElevationModelLayer, IMultipleGriddedElevationModelLayerRenderer, AbstractGriddedElevationModelLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type.replace(' ', '_') + ":add").toLowerCase();
    final String name = "Add " + type + " Style";
    menu.addMenuItem("add", name, iconName,
      (final IMultipleGriddedElevationModelLayerRenderer parentRenderer) -> {
        final ElevationModelLayer layer = parentRenderer.getLayer();
        final AbstractGriddedElevationModelLayerRenderer newRenderer = rendererFactory.apply(layer,
          parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      }, false);
  }

  public static void initMenus(final MenuFactory menu) {
    addAddMenuItem(menu, "Color", RasterizerGriddedElevationModelLayerRenderer::newColor);

    addAddMenuItem(menu, "Color Gradient",
      RasterizerGriddedElevationModelLayerRenderer::newColorGradient);

    addAddMenuItem(menu, "Hillshade", RasterizerGriddedElevationModelLayerRenderer::newHillshade);

    addAddMenuItem(menu, "Slope Color Gradient",
      RasterizerGriddedElevationModelLayerRenderer::newSlopeColorGradient);

    addAddMenuItem(menu, "Outline", BoundingBoxGriddedElevationModelLayerRenderer::new);

  }

  public static AbstractGriddedElevationModelLayerRenderer newColor(final ElevationModelLayer layer,
    final IMultipleGriddedElevationModelLayerRenderer parent) {
    final ColorGriddedElevationModelRasterizer rasterizer = new ColorGriddedElevationModelRasterizer();
    return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
  }

  public static RasterizerGriddedElevationModelLayerRenderer newColorGradient(
    final ElevationModelLayer layer, final IMultipleGriddedElevationModelLayerRenderer parent) {
    final ColorGradientGriddedElevationModelRasterizer rasterizer = new ColorGradientGriddedElevationModelRasterizer();
    final RasterizerGriddedElevationModelLayerRenderer renderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, parent, rasterizer);
    renderer.setOpacity(0.8f);
    return renderer;
  }

  public static RasterizerGriddedElevationModelLayerRenderer newHillshade(
    final ElevationModelLayer layer, final IMultipleGriddedElevationModelLayerRenderer parent) {
    final HillShadeGriddedElevationModelRasterizer rasterizer = new HillShadeGriddedElevationModelRasterizer();
    return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
  }

  public static RasterizerGriddedElevationModelLayerRenderer newSlopeColorGradient(
    final ElevationModelLayer layer, final IMultipleGriddedElevationModelLayerRenderer parent) {
    final SlopeColorGradientGriddedElevationModelRasterizer rasterizer = new SlopeColorGradientGriddedElevationModelRasterizer();
    final RasterizerGriddedElevationModelLayerRenderer renderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, parent, rasterizer);
    renderer.setOpacity(0.8f);
    return renderer;
  }

  private GriddedElevationModelImage image;

  private transient Thread worker;

  private GriddedElevationModelRasterizer rasterizer;

  private boolean redraw = true;

  private float opacity = 1;

  private boolean redrawInBackground = false;

  private GriddedElevationModelImageRasterizer joclRasterizer;

  private RasterizerGriddedElevationModelLayerRenderer() {
    super("rasterizerGriddedElevationModelLayerRenderer", "DEM Style", null);
  }

  public RasterizerGriddedElevationModelLayerRenderer(final ElevationModelLayer layer,
    final MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> parent) {
    this(layer, parent, null);
  }

  public RasterizerGriddedElevationModelLayerRenderer(final ElevationModelLayer layer,
    final MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> parent,
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

  @Override
  public RasterizerGriddedElevationModelLayerRenderer clone() {
    final RasterizerGriddedElevationModelLayerRenderer clone = (RasterizerGriddedElevationModelLayerRenderer)super.clone();
    if (this.rasterizer != null) {
      clone.rasterizer = this.rasterizer.clone();
    }
    clone.worker = null;
    clone.image = null;
    return clone;
  }

  @SuppressWarnings("unchecked")
  public void delete() {
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof MultipleLayerRenderer) {
      final MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> multiple = (MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer>)parent;
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
  public void render(final ViewRenderer view, final ElevationModelLayer layer,
    final BufferedGeoreferencedImage image) {
    // TODO cancel
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GriddedElevationModel elevationModel = getElevationModel();
        if (elevationModel != null) {
          synchronized (this) {
            if (this.rasterizer == null) {
              final ColorGradientGriddedElevationModelRasterizer rasterizer = new ColorGradientGriddedElevationModelRasterizer();
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
          if (this.joclRasterizer != null) {
            final BoundingBox modelBoundingBox = elevationModel.getBoundingBox();
            image.setBoundingBox(modelBoundingBox);
            final BufferedImage bufferedImage = image.getBufferedImage();
            if (bufferedImage != null) {
              this.joclRasterizer.rasterize(elevationModel, bufferedImage);
            }
            view.drawImage(image, true, this.opacity, null);
          } else if (!this.redrawInBackground || !view.isBackgroundDrawingEnabled()) {
            this.rasterizer.rasterize(image);
            view.drawImage(image, true, this.opacity, null);
          } else if (this.image.hasImage() && !(this.image.isCached() && this.redraw)) {
            view.drawImage(this.image, true, this.opacity, null);
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
          final BoundingBox boundingBox = layer.getBoundingBox();
          view.drawDifferentCoordinateSystem(boundingBox);
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

  @Override
  public void setLayer(final ElevationModelLayer layer) {
    this.redrawInBackground = !(layer instanceof TiledGriddedElevationModelLayer);
    super.setLayer(layer);
    if (this.rasterizer != null) {
      this.rasterizer.updateValues();
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
    if (rasterizer != null) {
      this.rasterizer = rasterizer;
      final String iconName = rasterizer.getIconName();
      setIcon(iconName);

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
      if (elevationModel == null) {
        rasterizer.updateValues();
      } else {
        rasterizer.setElevationModel(elevationModel);
      }
      this.joclRasterizer = JoclGriddedElevationModelImageRasterizer.newJoclRasterizer(rasterizer);
    }

    this.redraw = true;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "rasterizer", this.rasterizer);
    addToMap(map, "opacity", this.opacity);
    return map;
  }
}
