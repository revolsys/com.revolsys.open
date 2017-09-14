package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractMultipleRenderer extends AbstractRecordLayerRenderer
  implements MultipleLayerRenderer<AbstractRecordLayer, AbstractRecordLayerRenderer> {
  static {
    MenuFactory.addMenuInitializer(AbstractMultipleRenderer.class, menu -> {

      addAddMenuItem(menu, "Geometry", GeometryStyleRenderer::new);
      addAddMenuItem(menu, "Text", TextStyleRenderer::new);
      addAddMenuItem(menu, "Marker", MarkerStyleRenderer::new);
      addAddMenuItem(menu, "Multiple", MultipleRenderer::new);
      addAddMenuItem(menu, "Filter", FilterMultipleRenderer::new);
      addAddMenuItem(menu, "Scale", ScaleMultipleRenderer::new);

      addConvertMenuItem(menu, "Multiple", MultipleRenderer.class,
        AbstractMultipleRenderer::convertToMultipleStyle);
      addConvertMenuItem(menu, "Filter", FilterMultipleRenderer.class,
        AbstractMultipleRenderer::convertToFilterStyle);
      addConvertMenuItem(menu, "Scale", ScaleMultipleRenderer.class,
        AbstractMultipleRenderer::convertToScaleStyle);
    });
  }

  protected static void addAddMenuItem(final MenuFactory menu, final String type,
    final BiFunction<AbstractRecordLayer, AbstractMultipleRenderer, AbstractRecordLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type + "_add").toLowerCase();
    final String name = "Add " + type + " Style";
    Menus.addMenuItem(menu, "add", name, iconName,
      (final AbstractMultipleRenderer parentRenderer) -> {
        final AbstractRecordLayer layer = parentRenderer.getLayer();
        final AbstractRecordLayerRenderer newRenderer = rendererFactory.apply(layer,
          parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      }, false);
  }

  protected static void addConvertMenuItem(final MenuFactory menu, final String type,
    final Class<?> rendererClass, final Consumer<AbstractMultipleRenderer> consumer) {
    final String iconName = ("style_" + type + "_go").toLowerCase();
    final Predicate<AbstractMultipleRenderer> enabledFilter = (
      final AbstractMultipleRenderer renderer) -> {
      return renderer.getClass() != rendererClass;
    };
    final String name = "Convert to " + type + " Style";
    Menus.addMenuItem(menu, "convert", name, iconName, enabledFilter, consumer, false);
  }

  private List<AbstractRecordLayerRenderer> renderers = new ArrayList<>();

  public AbstractMultipleRenderer(final String type, final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    super(type, "Styles", layer, parent);

  }

  public AbstractMultipleRenderer(final String type, final String name) {
    super(type, name);
  }

  @Override
  public int addRenderer(final AbstractRecordLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

  @Override
  public int addRenderer(int index, final AbstractRecordLayerRenderer renderer) {
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
  public boolean canAddChild(final Object object) {
    return object instanceof AbstractRecordLayerRenderer;
  }

  @Override
  public AbstractMultipleRenderer clone() {
    final AbstractMultipleRenderer clone = (AbstractMultipleRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(this.renderers);
    for (final AbstractRecordLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
  }

  public FilterMultipleRenderer convertToFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final List<AbstractRecordLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(layer, parent);
    newRenderer.setProperties(style);
    newRenderer.setRenderers(JavaBeanUtil.clone(renderers));
    final String name = getName();
    if (name.equals("Multiple Style")) {
      newRenderer.setName("Filter Style");
    } else if (name.equals("Scale Style")) {
      newRenderer.setName("Filter Style");
    }
    replace(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRenderer convertToMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final List<AbstractRecordLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final MultipleRenderer newRenderer = new MultipleRenderer(layer, parent);
    newRenderer.setProperties(style);

    newRenderer.setRenderers(JavaBeanUtil.clone(renderers));
    final String name = getName();
    if (name.equals("Filter Style")) {
      newRenderer.setName("Multiple Style");
    } else if (name.equals("Scale Style")) {
      newRenderer.setName("Multiple Style");
    }
    replace(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer convertToScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final List<AbstractRecordLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer, parent);
    newRenderer.setProperties(style);
    newRenderer.setRenderers(JavaBeanUtil.clone(renderers));
    final String name = getName();
    if (name.equals("Filter Style")) {
      newRenderer.setName("Scale Style");
    } else if (name.equals("Multiple Style")) {
      newRenderer.setName("Scale Style");
    }
    replace(layer, parent, newRenderer);
    return newRenderer;
  }

  @Override
  public List<AbstractRecordLayerRenderer> getRenderers() {
    synchronized (this.renderers) {
      return new ArrayList<>(this.renderers);
    }
  }

  public boolean isEmpty() {
    return this.renderers.isEmpty();
  }

  @Override
  public boolean isSameLayer(final Layer layer) {
    return getLayer() == layer;
  }

  @Override
  public boolean isVisible(final LayerRecord record) {
    if (super.isVisible() && super.isVisible(record)) {
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(record)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void refreshIcon() {
    if (this.renderers != null) {
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.refreshIcon();
      }
    }
  }

  @Override
  public int removeRenderer(final AbstractRecordLayerRenderer renderer) {
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
  public void setLayer(final AbstractRecordLayer layer) {
    super.setLayer(layer);
    refreshIcon();
  }

  public void setRenderers(final List<? extends AbstractRecordLayerRenderer> renderers) {
    List<AbstractRecordLayerRenderer> oldValue;
    synchronized (this.renderers) {
      oldValue = Lists.toArray(this.renderers);
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<>(renderers);
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
      }
    }
    firePropertyChange("renderers", oldValue, this.renderers);
  }

  public void setStyles(final List<?> styles) {
    if (Property.hasValue(styles)) {
      final List<AbstractRecordLayerRenderer> renderers = new ArrayList<>();
      for (final Object childStyle : styles) {
        if (childStyle instanceof AbstractRecordLayerRenderer) {
          final AbstractRecordLayerRenderer renderer = (AbstractRecordLayerRenderer)childStyle;
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
    final List<AbstractRecordLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<>();
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      addToMap(map, "styles", rendererMaps);
    }
    return map;
  }
}
