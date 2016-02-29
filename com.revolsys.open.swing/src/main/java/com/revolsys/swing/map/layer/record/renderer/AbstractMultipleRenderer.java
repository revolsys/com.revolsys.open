package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.datatype.DataType;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;
import com.revolsys.util.function.Function2;

public abstract class AbstractMultipleRenderer extends AbstractRecordLayerRenderer {
  static {
    final MenuFactory menu = MenuFactory.getMenu(AbstractMultipleRenderer.class);

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
  }

  protected static void addAddMenuItem(final MenuFactory menu, final String type,
    final Function2<AbstractRecordLayer, AbstractMultipleRenderer, AbstractRecordLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type + "_add").toLowerCase();
    final String name = "Add " + type + " Style";
    Menus.addMenuItem(menu, "add", name, iconName,
      (final AbstractMultipleRenderer parentRenderer) -> {
        final AbstractRecordLayer layer = parentRenderer.getLayer();
        final AbstractRecordLayerRenderer newRenderer = rendererFactory.apply(layer,
          parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      });
  }

  protected static void addConvertMenuItem(final MenuFactory menu, final String type,
    final Class<?> rendererClass, final Consumer<AbstractMultipleRenderer> consumer) {
    final String iconName = ("style_" + type + "_go").toLowerCase();
    final Predicate<AbstractMultipleRenderer> enabledFilter = (
      final AbstractMultipleRenderer renderer) -> {
      return renderer.getClass() != rendererClass;
    };
    final String name = "Convert to " + type + " Style";
    Menus.addMenuItem(menu, "convert", name, iconName, enabledFilter, consumer);
  }

  private List<AbstractRecordLayerRenderer> renderers = new ArrayList<AbstractRecordLayerRenderer>();

  public AbstractMultipleRenderer(final String type, final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    super(type, "Styles", layer, parent);

  }

  public int addRenderer(final AbstractRecordLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

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

  public void addRendererEdit(final AbstractRecordLayerRenderer renderer) {
    addRenderer(-1, renderer);
    final Object item = MenuFactory.getMenuSource();
    if (item instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)item;
      final BaseTree tree = node.getTree();
      if (tree.isPropertyEqual("treeType", Project.class.getName())) {
        final AbstractRecordLayer layer = renderer.getLayer();
        layer.showRendererProperties(renderer);
      }
    }
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
  @SuppressWarnings("unchecked")
  public <V extends LayerRenderer<?>> V getRenderer(final List<String> path) {
    LayerRenderer<?> renderer = this;
    final int pathSize = path.size();
    for (int i = 0; i < pathSize; i++) {
      final String name = path.get(i);
      final String rendererName = renderer.getName();
      if (DataType.equal(name, rendererName)) {
        if (i < pathSize - 1) {
          final String childName = path.get(i + 1);
          if (renderer instanceof AbstractMultipleRenderer) {
            final AbstractMultipleRenderer multipleRenderer = (AbstractMultipleRenderer)renderer;
            renderer = multipleRenderer.getRenderer(childName);
          }
        }
      } else {
        return null;
      }
    }
    return (V)renderer;
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerRenderer<?>> V getRenderer(final String name) {
    if (Property.hasValue(name)) {
      for (final LayerRenderer<?> renderer : this.renderers) {
        final String rendererName = renderer.getName();
        if (DataType.equal(name, rendererName)) {
          return (V)renderer;
        }
      }
    }
    return null;
  }

  public List<AbstractRecordLayerRenderer> getRenderers() {
    synchronized (this.renderers) {
      return new ArrayList<AbstractRecordLayerRenderer>(this.renderers);
    }
  }

  public boolean hasRendererWithSameName(final LayerRenderer<?> renderer, final String name) {
    for (final AbstractRecordLayerRenderer otherRenderer : this.renderers) {
      if (renderer != otherRenderer) {
        final String layerName = otherRenderer.getName();
        if (name.equals(layerName)) {
          return true;
        }
      }
    }
    return false;
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
  public void setProperty(final String name, final Object value) {
    if ("styles".equals(name)) {
      final AbstractRecordLayer layer = getLayer();
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> styles = (List<Map<String, Object>>)value;
      if (styles != null) {
        final List<AbstractRecordLayerRenderer> renderers = new ArrayList<AbstractRecordLayerRenderer>();
        for (final Map<String, Object> childStyle : styles) {
          final AbstractRecordLayerRenderer renderer = AbstractRecordLayerRenderer
            .getRenderer(layer, this, childStyle);
          if (renderer != null) {
            renderers.add(renderer);
          }
        }
        setRenderers(renderers);
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setRenderers(final List<? extends AbstractRecordLayerRenderer> renderers) {
    synchronized (this.renderers) {
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<AbstractRecordLayerRenderer>(renderers);
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    final List<AbstractRecordLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<Map<String, Object>>();
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      MapSerializerUtil.add(map, "styles", rendererMaps);
    }
    return map;
  }
}
