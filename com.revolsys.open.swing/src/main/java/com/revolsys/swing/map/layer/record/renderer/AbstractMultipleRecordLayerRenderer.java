package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public abstract class AbstractMultipleRecordLayerRenderer extends AbstractRecordLayerRenderer
  implements MultipleLayerRenderer<AbstractRecordLayer, AbstractRecordLayerRenderer> {
  static {
    MenuFactory.addMenuInitializer(AbstractMultipleRecordLayerRenderer.class, menu -> {

      addAddMenuItem(menu, "Geometry", GeometryStyleRecordLayerRenderer::new);
      addAddMenuItem(menu, "Text", TextStyleRenderer::new);
      addAddMenuItem(menu, "Marker", MarkerStyleRenderer::new);
      addAddMenuItem(menu, "Multiple", MultipleRecordRenderer::new);
      addAddMenuItem(menu, "Filter", FilterMultipleRenderer::new);
      addAddMenuItem(menu, "Scale", ScaleMultipleRenderer::new);

      addConvertMenuItem(menu, "Multiple", MultipleRecordRenderer.class,
        AbstractMultipleRecordLayerRenderer::convertToMultipleStyle);
      addConvertMenuItem(menu, "Filter", FilterMultipleRenderer.class,
        AbstractMultipleRecordLayerRenderer::convertToFilterStyle);
      addConvertMenuItem(menu, "Scale", ScaleMultipleRenderer.class,
        AbstractMultipleRecordLayerRenderer::convertToScaleStyle);
    });
  }

  private static final AbstractRecordLayerRenderer[] EMPTY_ARRAY = new AbstractRecordLayerRenderer[0];

  private static final List<AbstractRecordLayerRenderer> EMPTY_LIST = Collections.emptyList();

  protected static void addAddMenuItem(final MenuFactory menu, final String type,
    final Function<AbstractMultipleRecordLayerRenderer, AbstractRecordLayerRenderer> rendererFactory) {
    final String iconName = ("style_" + type + ":add").toLowerCase();
    final String name = "Add " + type + " Style";
    menu.addMenuItem("add", name, iconName,
      (final AbstractMultipleRecordLayerRenderer parentRenderer) -> {
        final AbstractRecordLayerRenderer newRenderer = rendererFactory.apply(parentRenderer);
        parentRenderer.addRendererEdit(newRenderer);
      }, false);
  }

  protected static void addConvertMenuItem(final MenuFactory menu, final String type,
    final Class<?> rendererClass, final Consumer<AbstractMultipleRecordLayerRenderer> consumer) {
    final String iconName = ("style_" + type + "_go").toLowerCase();
    final Predicate<AbstractMultipleRecordLayerRenderer> enabledFilter = (
      final AbstractMultipleRecordLayerRenderer renderer) -> {
      return renderer.getClass() != rendererClass;
    };
    final String name = "Convert to " + type + " Style";
    menu.addMenuItem("convert", -1, name, iconName, enabledFilter, consumer, false);
  }

  protected AbstractRecordLayerRenderer[] renderers = EMPTY_ARRAY;

  public AbstractMultipleRecordLayerRenderer(final String type, final String name,
    final Icon icon) {
    super(type, name, icon);
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
      final AbstractRecordLayerRenderer[] oldRenderers = this.renderers;
      synchronized (this.renderers) {
        this.renderers = new AbstractRecordLayerRenderer[oldRenderers.length + 1];
        if (index < 0 || index > oldRenderers.length) {
          index = oldRenderers.length;
        }
        System.arraycopy(oldRenderers, 0, this.renderers, 0, index);
        System.arraycopy(oldRenderers, index, this.renderers, index + 1,
          oldRenderers.length - index);
        this.renderers[index] = renderer;

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
  public AbstractMultipleRecordLayerRenderer clone() {
    final AbstractMultipleRecordLayerRenderer clone = (AbstractMultipleRecordLayerRenderer)super.clone();
    clone.cloneRenderers(this);
    return clone;
  }

  protected void cloneRenderers(final AbstractMultipleRecordLayerRenderer original) {
    final AbstractRecordLayerRenderer[] oldRenderers = original.renderers;
    if (oldRenderers.length > 0) {
      final AbstractRecordLayerRenderer[] renderers = new AbstractRecordLayerRenderer[oldRenderers.length];
      for (int i = 0; i < oldRenderers.length; i++) {
        final AbstractRecordLayerRenderer oldRenderer = oldRenderers[i];
        final AbstractRecordLayerRenderer newRenderer = oldRenderer.clone();
        newRenderer.setParent(this);
        renderers[i] = newRenderer;
      }
      this.renderers = renderers;
    } else {
      this.renderers = EMPTY_ARRAY;
    }

  }

  public FilterMultipleRenderer convertToFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(parent);
    newRenderer.setProperties(style);
    newRenderer.cloneRenderers(this);
    String name = getName();
    if (name.equals("Multiple Style")) {
      name = "Filter Style";
    } else if (name.equals("Scale Style")) {
      name = "Filter Style";
    }
    replace(layer, parent, newRenderer);
    // Avoid having a 1 added to the name
    newRenderer.setName(name);
    return newRenderer;
  }

  public MultipleRecordRenderer convertToMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final MultipleRecordRenderer newRenderer = new MultipleRecordRenderer(parent);
    newRenderer.setProperties(style);
    newRenderer.cloneRenderers(this);
    String name = getName();
    if (name.equals("Filter Style")) {
      name = "Multiple Style";
    } else if (name.equals("Scale Style")) {
      name = "Multiple Style";
    }
    replace(layer, parent, newRenderer);
    // Avoid having a 1 added to the name
    newRenderer.setName(name);
    return newRenderer;
  }

  public ScaleMultipleRenderer convertToScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final Map<String, Object> style = toMap();
    style.remove("styles");
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(parent);
    newRenderer.setProperties(style);
    newRenderer.cloneRenderers(this);
    String name = getName();
    if (name.equals("Filter Style")) {
      name = "Scale Style";
    } else if (name.equals("Multiple Style")) {
      name = "Scale Style";
    }
    replace(layer, parent, newRenderer);
    // Avoid having a 1 added to the name
    newRenderer.setName(name);
    return newRenderer;
  }

  private List<LayerRecord> filterRecords(final ViewRenderer view, List<LayerRecord> records) {
    if (isHasFilter() && !records.isEmpty()) {
      records = Lists.filter(view, records, this.filterNoException);
    }
    return records;
  }

  public int getRendererCount() {
    return this.renderers.length;
  }

  @Override
  public List<AbstractRecordLayerRenderer> getRenderers() {
    final AbstractRecordLayerRenderer[] renderers = this.renderers;
    if (renderers.length == 0) {
      return EMPTY_LIST;
    } else {
      return Arrays.asList(renderers);
    }
  }

  public int indexOf(final AbstractRecordLayerRenderer renderer) {
    final AbstractRecordLayerRenderer[] renderers = this.renderers;
    for (int i = 0; i < renderers.length; i++) {
      if (renderers[i] == renderer) {
        return i;
      }
    }
    return -1;
  }

  public boolean isEmpty() {
    return this.renderers.length == 0;
  }

  @Override
  public boolean isSameLayer(final Layer layer) {
    return getLayer() == layer;
  }

  @Override
  public boolean isVisible(final LayerRecord record) {
    if (super.isVisible() && super.isVisible(record)) {
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
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
    final int index;
    boolean removed = false;
    synchronized (this.renderers) {
      index = indexOf(renderer);
      if (index != -1) {
        if (renderer.getParent() == this) {
          renderer.setParent(null);
        }
        final AbstractRecordLayerRenderer[] oldRenderers = this.renderers;
        this.renderers = new AbstractRecordLayerRenderer[oldRenderers.length - 1];
        System.arraycopy(oldRenderers, 0, this.renderers, 0, index);
        System.arraycopy(oldRenderers, index + 1, this.renderers, index,
          oldRenderers.length - index - 1);

        removed = true;
      }
    }
    if (removed) {
      firePropertyChange("renderers", index, renderer, null);
    }
    return index;
  }

  protected abstract void renderMultipleRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records);

  protected abstract void renderMultipleSelectedRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records);

  @Override
  protected final void renderRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    List<LayerRecord> records) {
    if (isVisible(view)) {
      records = filterRecords(view, records);
      renderMultipleRecords(view, layer, records);
    }
  }

  @Override
  protected final void renderSelectedRecordsDo(final ViewRenderer view,
    final AbstractRecordLayer layer, List<LayerRecord> records) {
    if (isVisible(view)) {
      records = filterRecords(view, records);

      renderMultipleSelectedRecords(view, layer, records);
    }
  }

  @Override
  public void setLayer(final AbstractRecordLayer layer) {
    super.setLayer(layer);
    synchronized (this.renderers) {
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setLayer(layer);
      }
    }
    refreshIcon();
  }

  public void setRenderers(final List<? extends AbstractRecordLayerRenderer> renderers) {
    List<AbstractRecordLayerRenderer> oldValue;
    synchronized (this.renderers) {
      oldValue = Arrays.asList(this.renderers);
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null || renderers.isEmpty()) {
        this.renderers = EMPTY_ARRAY;
      } else {
        this.renderers = renderers.toArray(new AbstractRecordLayerRenderer[renderers.size()]);
        for (final AbstractRecordLayerRenderer renderer : this.renderers) {
          renderer.setParent(this);
        }
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    final AbstractRecordLayerRenderer[] renderers = this.renderers;
    if (renderers.length > 0) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<>();
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      addToMap(map, "styles", rendererMaps);
    }
    return map;
  }
}
