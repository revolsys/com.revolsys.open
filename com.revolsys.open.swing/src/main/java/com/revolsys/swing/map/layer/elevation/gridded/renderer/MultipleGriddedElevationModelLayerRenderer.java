package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class MultipleGriddedElevationModelLayerRenderer
  extends AbstractGriddedElevationModelLayerRenderer
  implements IMultipleGriddedElevationModelLayerRenderer {

  private static final Icon ICON = Icons.getIcon("folder:palette");

  static {
    MenuFactory.addMenuInitializer(MultipleGriddedElevationModelLayerRenderer.class, menu -> {
      RasterizerGriddedElevationModelLayerRenderer.initMenus(menu);
    });
  }

  private List<AbstractGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();

  private MultipleGriddedElevationModelLayerRenderer() {
    super("multipleGriddedElevationModelLayerRenderer", "Styles", ICON);
  }

  public MultipleGriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer) {
    this();
    setLayer(layer);
    final HillShadeGriddedElevationModelRasterizer hillshadeRasterizer = new HillShadeGriddedElevationModelRasterizer();
    final RasterizerGriddedElevationModelLayerRenderer hillshadeRenderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, this, hillshadeRasterizer);
    addRenderer(hillshadeRenderer);
    final RasterizerGriddedElevationModelLayerRenderer colorRampRenderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, this);
    colorRampRenderer.setOpacity(0.8f);
    addRenderer(colorRampRenderer);
  }

  public MultipleGriddedElevationModelLayerRenderer(final GriddedElevationModelLayer layer,
    final AbstractGriddedElevationModelLayerRenderer renderer) {
    this();
    setLayer(layer);
    addRenderer(renderer);
  }

  public MultipleGriddedElevationModelLayerRenderer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public int addRenderer(final AbstractGriddedElevationModelLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

  @Override
  public int addRenderer(int index, final AbstractGriddedElevationModelLayerRenderer renderer) {
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
  public boolean canAddChild(final Object object) {
    return object instanceof AbstractGriddedElevationModelLayerRenderer;
  }

  @Override
  public MultipleGriddedElevationModelLayerRenderer clone() {
    final MultipleGriddedElevationModelLayerRenderer clone = (MultipleGriddedElevationModelLayerRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(this.renderers);
    for (final AbstractGriddedElevationModelLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
  }

  @Override
  public List<AbstractGriddedElevationModelLayerRenderer> getRenderers() {
    synchronized (this.renderers) {
      return new ArrayList<>(this.renderers);
    }
  }

  @Override
  public boolean hasRendererWithSameName(final LayerRenderer<?> renderer, final String name) {
    for (final AbstractGriddedElevationModelLayerRenderer otherRenderer : this.renderers) {
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
  public void refresh() {
    for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
      renderer.refresh();
    }
  }

  @Override
  public void refreshIcon() {
    if (this.renderers != null) {
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.refreshIcon();
      }
    }
  }

  @Override
  public int removeRenderer(final AbstractGriddedElevationModelLayerRenderer renderer) {
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
  public void render(final ViewRenderer view, final ElevationModelLayer layer,
    final BufferedGeoreferencedImage image) {
    final List<AbstractGriddedElevationModelLayerRenderer> renderers = getRenderers();
    for (final AbstractGriddedElevationModelLayerRenderer renderer : view.cancellable(renderers)) {
      try {
        if (renderer.isVisible(view)) {
          renderer.render(view, layer, image);
        }
      } catch (final Exception e) {
        Logs.error(this, "Unable to render:" + renderer, e);
      }
    }
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
    for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
      renderer.setElevationModel(elevationModel);
    }
  }

  @Override
  public void setLayer(final ElevationModelLayer layer) {
    super.setLayer(layer);
    synchronized (this.renderers) {
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setLayer(layer);
      }
    }
    refreshIcon();
  }

  public void setRenderers(
    final List<? extends AbstractGriddedElevationModelLayerRenderer> renderers) {
    List<AbstractGriddedElevationModelLayerRenderer> oldValue;
    synchronized (this.renderers) {
      oldValue = Lists.toArray(this.renderers);
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<>(renderers);
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
      }
    }
    firePropertyChange("renderers", oldValue, this.renderers);
  }

  public void setStyles(final List<?> styles) {
    if (Property.hasValue(styles)) {
      final List<AbstractGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();
      for (final Object childStyle : styles) {
        if (childStyle instanceof AbstractGriddedElevationModelLayerRenderer) {
          final AbstractGriddedElevationModelLayerRenderer renderer = (AbstractGriddedElevationModelLayerRenderer)childStyle;
          renderers.add(renderer);
        } else {
          Logs.error(this, "Cannot create renderer for: " + childStyle);
        }
      }
      setRenderers(renderers);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    final List<AbstractGriddedElevationModelLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<>();
      for (final AbstractGriddedElevationModelLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      addToMap(map, "styles", rendererMaps);
    }
    return map;
  }
}
