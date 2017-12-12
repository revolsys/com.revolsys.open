package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorRampGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.logging.Logs;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.Cancellable;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class TiledMultipleGriddedElevationModelLayerRenderer extends
  AbstractTiledLayerRenderer<GriddedElevationModel, TiledGriddedElevationModelLayerTile> implements
  MultipleLayerRenderer<ElevationModelLayer, RasterizerGriddedElevationModelLayerRenderer>,
  GriddedElevationModelZRange {
  static {
    MenuFactory.addMenuInitializer(TiledMultipleGriddedElevationModelLayerRenderer.class, menu -> {
      Menus.addMenuItem(menu, "layer", "View/Edit Style", "palette",
        ((Predicate<TiledMultipleGriddedElevationModelLayerRenderer>)TiledMultipleGriddedElevationModelLayerRenderer::isEditing)
          .negate(),
        TiledMultipleGriddedElevationModelLayerRenderer::showProperties, false);

      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(true, null,
          TiledMultipleGriddedElevationModelLayerRenderer::getMinimumScale,
          TiledMultipleGriddedElevationModelLayerRenderer::setMinimumScale));
      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(false, null,
          TiledMultipleGriddedElevationModelLayerRenderer::getMaximumScale,
          TiledMultipleGriddedElevationModelLayerRenderer::setMaximumScale));

      addAddMenuItem(menu, "Colour", (layer, parent) -> {
        final ColorGriddedElevationModelRasterizer rasterizer = new ColorGriddedElevationModelRasterizer();
        return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
      });
      addAddMenuItem(menu, "Colour Ramp", (layer, parent) -> {
        final ColorRampGriddedElevationModelRasterizer rasterizer = new ColorRampGriddedElevationModelRasterizer();
        return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
      });
      addAddMenuItem(menu, "Hillshade", (layer, parent) -> {
        final HillShadeGriddedElevationModelRasterizer rasterizer = new HillShadeGriddedElevationModelRasterizer();
        return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
      });
    });
  }

  protected static void addAddMenuItem(final MenuFactory menu, final String type,
    final BiFunction<TiledGriddedElevationModelLayer, TiledMultipleGriddedElevationModelLayerRenderer, RasterizerGriddedElevationModelLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type + ":add").toLowerCase();
    final String name = "Add " + type + " Style";
    Menus.addMenuItem(menu, "add", name, iconName,
      (final TiledMultipleGriddedElevationModelLayerRenderer parentRenderer) -> {
        final TiledGriddedElevationModelLayer layer = parentRenderer.getLayer();
        final RasterizerGriddedElevationModelLayerRenderer newRenderer = rendererFactory
          .apply(layer, parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      }, false);
  }

  private List<RasterizerGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();

  private double minZ = Double.NaN;

  private double maxZ = Double.NaN;

  private TiledMultipleGriddedElevationModelLayerRenderer() {
    super("tiledMultipleGriddedElevationModelLayerRenderer", "Styles");
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final TiledGriddedElevationModelLayer layer) {
    this();
    setLayer(layer);
    addRenderer(new RasterizerGriddedElevationModelLayerRenderer(layer, this));
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final TiledGriddedElevationModelLayer layer,
    final RasterizerGriddedElevationModelLayerRenderer renderer) {
    this();
    setLayer(layer);
    addRenderer(renderer);
  }

  @Override
  public int addRenderer(int index, final RasterizerGriddedElevationModelLayerRenderer renderer) {
    if (renderer == null) {
      return -1;
    } else {
      final String originalName = renderer.getName();
      String name = originalName;
      int i = 1;
      while (hasRendererWithSameName(renderer, name)) {
        name = originalName + i;
        i++;
      }
      renderer.setName(name);
      renderer.setParent(this);
      synchronized (this.renderers) {
        if (index < 0) {
          index = this.renderers.size();
        }
        this.renderers.add(index, renderer);
      }
      firePropertyChange("renderers", index, null, renderer);
      return index;
    }
  }

  @Override
  public int addRenderer(final RasterizerGriddedElevationModelLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

  @Override
  public boolean canAddChild(final Object object) {
    return object instanceof RasterizerGriddedElevationModelLayerRenderer;
  }

  @Override
  public TiledMultipleGriddedElevationModelLayerRenderer clone() {
    final TiledMultipleGriddedElevationModelLayerRenderer clone = (TiledMultipleGriddedElevationModelLayerRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(this.renderers);
    for (final RasterizerGriddedElevationModelLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
  }

  @Override
  public TiledGriddedElevationModelLayer getLayer() {
    return (TiledGriddedElevationModelLayer)super.getLayer();
  }

  @Override
  public double getMaxZ() {
    return this.maxZ;
  }

  @Override
  public double getMinZ() {
    return this.minZ;
  }

  @Override
  public List<RasterizerGriddedElevationModelLayerRenderer> getRenderers() {
    synchronized (this.renderers) {
      return new ArrayList<>(this.renderers);
    }
  }

  @Override
  public boolean hasRendererWithSameName(final LayerRenderer<?> renderer, final String name) {
    for (final RasterizerGriddedElevationModelLayerRenderer otherRenderer : this.renderers) {
      if (renderer != otherRenderer) {
        final String layerName = otherRenderer.getName();
        if (name.equals(layerName)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return this.renderers.isEmpty();
  }

  @Override
  public boolean isSameLayer(final Layer layer) {
    return getLayer() == layer;
  }

  @Override
  public Form newStylePanel() {
    return new TiledMultipleGriddedElevationModelStylePanel(this);
  }

  @Override
  public int removeRenderer(final RasterizerGriddedElevationModelLayerRenderer renderer) {
    boolean removed = false;
    synchronized (this.renderers) {
      final int index = this.renderers.indexOf(renderer);
      if (index != -1) {
        if (renderer.getParent() == this) {
          renderer.setParent(null);
        }
        removed = this.renderers.remove(renderer);
      }
      if (removed) {
        firePropertyChange("renderers", index, renderer, null);
      }
      return index;
    }
  }

  @Override
  protected void renderTile(final Viewport2D viewport, final Cancellable cancellable,
    final TiledGriddedElevationModelLayerTile tile) {
    final Graphics2D graphics = viewport.getGraphics();
    if (graphics != null) {
      final GriddedElevationModel elevationModel = tile.getElevationModel();
      if (elevationModel != null) {
        final TiledGriddedElevationModelLayer layer = getLayer();
        final List<RasterizerGriddedElevationModelLayerRenderer> renderers = getRenderers();
        for (final RasterizerGriddedElevationModelLayerRenderer renderer : cancellable
          .cancellable(renderers)) {
          final long scaleForVisible = (long)viewport.getScaleForVisible();
          if (renderer.isVisible(scaleForVisible)) {
            renderer.setElevationModel(elevationModel);
            renderer.render(viewport, cancellable, layer);
          }
        }
      }
    }
  }

  @Override
  public void setLayer(
    final AbstractTiledLayer<GriddedElevationModel, TiledGriddedElevationModelLayerTile> layer) {
    super.setLayer(layer);
    updateBoundingBox();
  }

  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
    updateBoundingBox();
  }

  public void setMinZ(final double minZ) {
    this.minZ = minZ;
    updateBoundingBox();
  }

  public void setRenderers(
    final List<? extends RasterizerGriddedElevationModelLayerRenderer> renderers) {
    List<RasterizerGriddedElevationModelLayerRenderer> oldValue;
    synchronized (this.renderers) {
      oldValue = Lists.toArray(this.renderers);
      for (final RasterizerGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<>(renderers);
      for (final RasterizerGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
      }
    }
    firePropertyChange("renderers", oldValue, this.renderers);
  }

  public void setStyles(final List<?> styles) {
    if (Property.hasValue(styles)) {
      final List<RasterizerGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();
      for (final Object childStyle : styles) {
        if (childStyle instanceof RasterizerGriddedElevationModelLayerRenderer) {
          final RasterizerGriddedElevationModelLayerRenderer renderer = (RasterizerGriddedElevationModelLayerRenderer)childStyle;
          renderers.add(renderer);
        } else {
          Logs.error(this, "Cannot create renderer for: " + childStyle);
        }
      }
      setRenderers(renderers);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    final List<RasterizerGriddedElevationModelLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<>();
      for (final RasterizerGriddedElevationModelLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      addToMap(map, "styles", rendererMaps);
    }
    addToMap(map, "minZ", this.minZ);
    addToMap(map, "maxZ", this.maxZ);
    return map;
  }

  public void updateBoundingBox() {
    final TiledGriddedElevationModelLayer layer = getLayer();
    if (layer != null) {
      final BoundingBox boundingBox = layer.getBoundingBox();
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      final BoundingBox newBoundingBox = boundingBox.newBoundingBox(minX, minY, this.minZ, maxX,
        maxY, this.maxZ);
      layer.setBoundingBox(newBoundingBox);
    }
  }
}
