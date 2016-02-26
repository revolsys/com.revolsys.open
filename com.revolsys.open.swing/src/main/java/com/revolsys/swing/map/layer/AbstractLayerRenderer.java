package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public abstract class AbstractLayerRenderer<T extends Layer> extends
  BaseObjectWithPropertiesAndChange implements LayerRenderer<T>, PropertyChangeListener, Cloneable {

  private static final Icon ICON = Icons.getIcon("palette");

  private boolean editing = true;

  private Icon icon = ICON;

  private Reference<T> layer;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private String name;

  private LayerRenderer<?> parent;

  private final String type;

  private boolean visible = true;

  private boolean open = false;

  public AbstractLayerRenderer(final String type, String name, final T layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    this(type, layer);
    final Number minimumScale = getValue(style, "minimumScale");
    if (minimumScale != null) {
      setMinimumScale(minimumScale.longValue());
    }
    final Number maximumScale = getValue(style, "maximumScale");
    if (maximumScale != null) {
      setMaximumScale(maximumScale.longValue());
    }
    final Boolean visible = getValue(style, "visible");
    if (visible != null) {
      this.visible = visible;
    }
    final String styleName = (String)style.remove("name");
    if (Property.hasValue(styleName)) {
      name = styleName;
    }
    setName(name);
    setParent(parent);
    final Boolean open = getValue(style, "open");
    if (open != null) {
      this.open = open;
    }
  }

  public AbstractLayerRenderer(final String type, final T layer) {
    this.type = type;
    this.layer = new WeakReference<T>(layer);
    this.name = CaseConverter.toCapitalizedWords(type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractLayerRenderer<T> clone() {
    final AbstractLayerRenderer<T> clone = (AbstractLayerRenderer<T>)super.clone();
    clone.layer = new WeakReference<T>(this.layer.get());
    clone.parent = null;
    clone.editing = false;
    return clone;
  }

  @Override
  public Icon getIcon() {
    return this.icon;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getLayer() {
    final LayerRenderer<?> parent = getParent();
    if (parent == null) {
      if (this.layer == null) {
        return null;
      } else {
        return this.layer.get();
      }
    } else {
      return (T)parent.getLayer();
    }
  }

  public long getMaximumScale() {
    return this.maximumScale;
  }

  public long getMinimumScale() {
    return this.minimumScale;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public LayerRenderer<?> getParent() {
    return this.parent;
  }

  @Override
  public List<String> getPathNames() {
    final LinkedList<String> names = new LinkedList<String>();
    final String name = getName();
    names.add(name);
    for (LayerRenderer<?> parent = getParent(); parent != null; parent = parent.getParent()) {
      final String parentName = parent.getName();
      names.addFirst(parentName);
    }
    return names;
  }

  @Override
  public List<LayerRenderer<?>> getPathRenderers() {
    final LinkedList<LayerRenderer<?>> renderers = new LinkedList<LayerRenderer<?>>();
    renderers.add(this);
    for (LayerRenderer<?> parent = getParent(); parent != null; parent = parent.getParent()) {
      renderers.addFirst(parent);
    }
    return renderers;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends LayerRenderer<?>> V getRenderer(final List<String> path) {
    if (path.isEmpty()) {
      return null;
    } else if (path.get(0).equals(getName())) {
      return (V)this;
    }
    return null;
  }

  public String getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  protected <V> V getValue(final Map<String, Object> map, final String name) {
    final Object value = map.get(name);
    return (V)value;
  }

  @Override
  public boolean isEditing() {
    if (this.parent == null) {
      return this.editing;
    } else {
      return this.parent.isEditing();
    }
  }

  @Override
  public boolean isOpen() {
    return this.open;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  public boolean isVisible(final double scale) {
    if (isVisible()) {
      final long longScale = Math.round(scale);
      final long min = getMinimumScale();
      final long max = getMaximumScale();
      if (min >= longScale && longScale >= max) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Form newStylePanel() {
    return new BaseStylePanel(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    firePropertyChange(event);
  }

  @Override
  public final void render(final Viewport2D viewport) {
    final T layer = getLayer();
    if (layer != null) {
      final double scaleForVisible = viewport.getScaleForVisible();
      if (isVisible(scaleForVisible)) {
        render(viewport, layer);
      }
    }
  }

  public abstract void render(Viewport2D viewport, T layer);

  @Override
  public void setEditing(final boolean editing) {
    final boolean oldValue = this.editing;
    this.editing = editing;
    firePropertyChange("editing", oldValue, this.layer);
  }

  public void setIcon(final Icon icon) {
    final Object oldValue = this.icon;
    this.icon = icon;
    firePropertyChange("icon", oldValue, icon);
  }

  @Override
  public void setLayer(final T layer) {
    final Object oldValue;
    if (this.layer == null) {
      oldValue = null;
    } else {
      oldValue = this.layer.get();
    }
    this.layer = new WeakReference<T>(layer);
    firePropertyChange("layer", oldValue, layer);
  }

  public long setMaximumScale(final long maximumScale) {
    final long oldValue = this.maximumScale;
    if (maximumScale >= 0) {
      this.maximumScale = maximumScale;
      firePropertyChange("maximumScale", oldValue, maximumScale);
    }
    return oldValue;
  }

  public long setMinimumScale(final long minimumScale) {
    final long oldValue = this.minimumScale;
    this.minimumScale = minimumScale;
    firePropertyChange("minimumScale", oldValue, minimumScale);
    return oldValue;
  }

  public void setName(final String name) {
    final String oldName = getName();
    if (Property.hasValue(name)) {
      this.name = name;
    } else {
      this.name = CaseConverter.toCapitalizedWords(this.type);
    }
    firePropertyChange("name", oldName, this.name);
  }

  @Override
  public void setOpen(final boolean open) {
    final boolean oldValue = this.open;
    this.open = open;
    firePropertyChange("open", oldValue, this.open);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setParent(final LayerRenderer<?> parent) {
    final LayerRenderer<?> oldValue = this.parent;
    Property.removeListener(this, oldValue);
    this.parent = parent;
    if (parent != null) {
      setLayer((T)parent.getLayer());
    }
    Property.addListener(this, parent);
    firePropertyChange("parent", oldValue, parent);
  }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    firePropertyChange("visible", oldVisible, visible);
  }

  public void showProperties() {
    final T layer = getLayer();
    if (layer != null) {
      layer.showRendererProperties(this);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    MapSerializerUtil.add(map, "type", this.type);
    MapSerializerUtil.add(map, "name", this.name);
    MapSerializerUtil.add(map, "visible", this.visible);
    MapSerializerUtil.add(map, "maximumScale", this.maximumScale);
    MapSerializerUtil.add(map, "minimumScale", this.minimumScale);
    MapSerializerUtil.add(map, "open", this.open);
    return map;
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.name)) {
      return this.name;
    } else {
      return this.type;
    }
  }

}
