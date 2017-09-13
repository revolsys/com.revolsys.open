package com.revolsys.swing.map.layer.elevation.gridded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColourGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.Cancellable;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class MultipleGriddedElevationModelLayerRenderer
  extends AbstractGriddedElevationModelLayerRenderer implements
  MultipleLayerRenderer<GriddedElevationModelLayer, RasterizerGriddedElevationModelLayerRenderer> {
  static {
    MenuFactory.addMenuInitializer(MultipleGriddedElevationModelLayerRenderer.class, menu -> {

      addAddMenuItem(menu, "Colour", (layer, parent) -> {
        final GriddedElevationModel elevationModel = layer.getElevationModel();
        final ColourGriddedElevationModelRasterizer rasterizer = new ColourGriddedElevationModelRasterizer(
          elevationModel);
        return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
      });
      addAddMenuItem(menu, "Hillshade", (layer, parent) -> {
        final GriddedElevationModel elevationModel = layer.getElevationModel();
        final HillShadeGriddedElevationModelRasterizer rasterizer = new HillShadeGriddedElevationModelRasterizer(
          elevationModel);
        return new RasterizerGriddedElevationModelLayerRenderer(layer, parent, rasterizer);
      });
    });
  }

  protected static void addAddMenuItem(final MenuFactory menu, final String type,
    final BiFunction<GriddedElevationModelLayer, MultipleGriddedElevationModelLayerRenderer, RasterizerGriddedElevationModelLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type + "_add").toLowerCase();
    final String name = "Add " + type + " Style";
    Menus.addMenuItem(menu, "add", name, iconName,
      (final MultipleGriddedElevationModelLayerRenderer parentRenderer) -> {
        final GriddedElevationModelLayer layer = parentRenderer.getLayer();
        final RasterizerGriddedElevationModelLayerRenderer newRenderer = rendererFactory
          .apply(layer, parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      }, false);
  }

  private List<RasterizerGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();

  private MultipleGriddedElevationModelLayerRenderer() {
    super("multipleGriddedElevationModelLayerRenderer", "Styles");
  }

  public MultipleGriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer) {
    this();
    setLayer(layer);
    addRenderer(new RasterizerGriddedElevationModelLayerRenderer(layer, this));
  }

  public MultipleGriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer,
    final RasterizerGriddedElevationModelLayerRenderer renderer) {
    this();
    setLayer(layer);
    addRenderer(renderer);
  }

  public MultipleGriddedElevationModelLayerRenderer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
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
        final GriddedElevationModel elevationModel = getElevationModel();
        renderer.setElevationModel(elevationModel);
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
  public MultipleGriddedElevationModelLayerRenderer clone() {
    final MultipleGriddedElevationModelLayerRenderer clone = (MultipleGriddedElevationModelLayerRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(this.renderers);
    for (final RasterizerGriddedElevationModelLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
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
  public void refreshIcon() {
    if (this.renderers != null) {
      for (final RasterizerGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.refreshIcon();
      }
    }
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
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final GriddedElevationModelLayer layer) {
    final List<RasterizerGriddedElevationModelLayerRenderer> renderers = getRenderers();
    for (final RasterizerGriddedElevationModelLayerRenderer renderer : cancellable
      .cancellable(renderers)) {
      final long scaleForVisible = (long)viewport.getScaleForVisible();
      if (renderer.isVisible(scaleForVisible)) {
        renderer.render(viewport, cancellable, layer);
      }
    }
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
    for (final RasterizerGriddedElevationModelLayerRenderer renderer : this.renderers) {
      renderer.setElevationModel(elevationModel);
    }
  }

  @Override
  public void setLayer(final GriddedElevationModelLayer layer) {
    super.setLayer(layer);
    refreshIcon();
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
    return map;
  }
}
