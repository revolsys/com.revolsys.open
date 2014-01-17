package com.revolsys.swing.map.layer.dataobject.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractMultipleRenderer extends
  AbstractDataObjectLayerRenderer {
  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractMultipleRenderer.class);

    for (final String type : Arrays.asList("Geometry", "Text", "Marker",
      "Multiple", "Filter", "Scale")) {
      final String iconName = ("style_" + type + "_add").toLowerCase();
      final ImageIcon icon = SilkIconLoader.getIcon(iconName);
      final InvokeMethodAction action = TreeItemRunnable.createAction("Add "
        + type + " Style", icon, null, "add" + type + "Style");
      menu.addMenuItem("add", action);
    }

    addMenuItem(menu, "Multiple", MultipleRenderer.class);
    addMenuItem(menu, "Filter", FilterMultipleRenderer.class);
    addMenuItem(menu, "Scale", ScaleMultipleRenderer.class);
  }

  protected static void addMenuItem(final MenuFactory menu, final String type,
    final Class<?> rendererClass) {
    final String iconName = ("style_" + type + "_go").toLowerCase();
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);

    final TreeItemPropertyEnableCheck enableCheck = new TreeItemPropertyEnableCheck(
      "class", rendererClass, true);
    final InvokeMethodAction action = TreeItemRunnable.createAction(
      "Convert to " + type + " Style", icon, enableCheck, "convertTo" + type
        + "Style");
    menu.addMenuItem("convert", action);
  }

  private List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();

  public AbstractMultipleRenderer(final String type,
    final AbstractDataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super(type, "Styles", layer, parent, style);
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> styles = (List<Map<String, Object>>)style.get("styles");
    if (styles != null) {
      final List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
      for (final Map<String, Object> childStyle : styles) {
        final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(
          layer, this, childStyle);
        renderers.add(renderer);
      }
      setRenderers(renderers);
    }
  }

  public FilterMultipleRenderer addFilterStyle() {
    final FilterMultipleRenderer renderer = new FilterMultipleRenderer(
      getLayer(), this);
    addRenderer(renderer);
    return renderer;
  }

  public GeometryStyleRenderer addGeometryStyle() {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(
      getLayer(), this);
    addRenderer(renderer);
    return renderer;
  }

  public MarkerStyleRenderer addMarkerStyle() {
    final MarkerStyleRenderer renderer = new MarkerStyleRenderer(getLayer(),
      this);
    addRenderer(renderer);
    return renderer;
  }

  public MultipleRenderer addMultipleStyle() {
    final MultipleRenderer renderer = new MultipleRenderer(getLayer(), this);
    addRenderer(renderer);
    return renderer;
  }

  public int addRenderer(final AbstractDataObjectLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

  public int addRenderer(int index,
    final AbstractDataObjectLayerRenderer renderer) {
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
      synchronized (renderers) {
        if (index < 0) {
          index = this.renderers.size();
        }
        this.renderers.add(index, renderer);
      }
      firePropertyChange("renderers", index, null, renderer);
      return index;
    }
  }

  public ScaleMultipleRenderer addScaleStyle() {
    final ScaleMultipleRenderer renderer = new ScaleMultipleRenderer(
      getLayer(), this);
    addRenderer(renderer);
    return renderer;
  }

  public TextStyleRenderer addTextStyle() {
    final TextStyleRenderer renderer = new TextStyleRenderer(getLayer(), this);
    addRenderer(renderer);
    return renderer;
  }

  @Override
  public AbstractMultipleRenderer clone() {
    final AbstractMultipleRenderer clone = (AbstractMultipleRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(renderers);
    for (final AbstractDataObjectLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
  }

  public FilterMultipleRenderer convertToFilterStyle() {
    final AbstractDataObjectLayer layer = getLayer();
    final List<AbstractDataObjectLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(
      layer, parent, style);
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
    final AbstractDataObjectLayer layer = getLayer();
    final List<AbstractDataObjectLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final MultipleRenderer newRenderer = new MultipleRenderer(layer, parent,
      style);
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
    final AbstractDataObjectLayer layer = getLayer();
    final List<AbstractDataObjectLayerRenderer> renderers = getRenderers();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer,
      parent, style);
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
      if (EqualsRegistry.equal(name, rendererName)) {
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
    if (StringUtils.hasText(name)) {
      for (final LayerRenderer<?> renderer : this.renderers) {
        final String rendererName = renderer.getName();
        if (EqualsRegistry.equal(name, rendererName)) {
          return (V)renderer;
        }
      }
    }
    return null;
  }

  public List<AbstractDataObjectLayerRenderer> getRenderers() {
    synchronized (renderers) {
      return new ArrayList<AbstractDataObjectLayerRenderer>(this.renderers);
    }
  }

  public boolean hasRendererWithSameName(final LayerRenderer<?> renderer,
    final String name) {
    for (final AbstractDataObjectLayerRenderer otherRenderer : renderers) {
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
  public boolean isVisible(final LayerDataObject object) {
    if (super.isVisible() && super.isVisible(object)) {
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(object)) {
          return true;
        }
      }
    }
    return false;
  }

  public int removeRenderer(final AbstractDataObjectLayerRenderer renderer) {
    boolean removed = false;
    synchronized (renderers) {
      final int index = renderers.indexOf(renderer);
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

  public void setRenderers(
    final List<? extends AbstractDataObjectLayerRenderer> renderers) {
    synchronized (this.renderers) {
      for (final AbstractDataObjectLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>(renderers);
      for (final AbstractDataObjectLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    final List<AbstractDataObjectLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<Map<String, Object>>();
      for (final AbstractDataObjectLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      MapSerializerUtil.add(map, "styles", rendererMaps);
    }
    return map;
  }
}
