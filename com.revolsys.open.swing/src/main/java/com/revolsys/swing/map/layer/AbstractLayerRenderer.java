package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;
import com.revolsys.util.Cancellable;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public abstract class AbstractLayerRenderer<T extends Layer> extends
  BaseObjectWithPropertiesAndChange implements LayerRenderer<T>, PropertyChangeListener, Cloneable {

  private static final Icon ICON = Icons.getIcon("palette");

  private boolean editing = true;

  private Icon icon = ICON;

  private T layer;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private String name;

  private LayerRenderer<?> parent;

  private final String type;

  private boolean visible = true;

  private boolean open = false;

  public AbstractLayerRenderer(final String type) {
    this(type, (String)null);
  }

  public AbstractLayerRenderer(final String type, final String name) {
    this.type = type;
    setName(name);
  }

  public AbstractLayerRenderer(final String type, final String name, final T layer,
    final LayerRenderer<?> parent) {
    this(type, name);
    setLayer(layer);
    setParent(parent);
  }

  public AbstractLayerRenderer(final String type, final T layer) {
    this(type);
    this.layer = layer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractLayerRenderer<T> clone() {
    final AbstractLayerRenderer<T> clone = (AbstractLayerRenderer<T>)super.clone();
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
      return this.layer;
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
    final LinkedList<String> names = new LinkedList<>();
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
    final LinkedList<LayerRenderer<?>> renderers = new LinkedList<>();
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
  public final void render(final Viewport2D viewport, final Cancellable cancellable) {
    final T layer = getLayer();
    if (layer != null) {
      final double scaleForVisible = viewport.getScaleForVisible();
      if (isVisible(scaleForVisible)) {
        render(viewport, cancellable, layer);
      }
    }
  }

  public abstract void render(Viewport2D viewport, Cancellable cancellable, T layer);

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
    final Object oldValue = this.layer;
    this.layer = layer;
    firePropertyChange("layer", oldValue, layer);
  }

  public void setMaximumScale(long maximumScale) {
    if (maximumScale < 0) {
      maximumScale = 0;
    }
    final long oldValue = this.maximumScale;
    this.maximumScale = maximumScale;
    firePropertyChange("maximumScale", oldValue, maximumScale);
  }

  public void setMinimumScale(long minimumScale) {
    if (minimumScale <= 0) {
      minimumScale = Long.MAX_VALUE;
    }
    final long oldValue = this.minimumScale;
    this.minimumScale = minimumScale;
    firePropertyChange("minimumScale", oldValue, minimumScale);
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
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, this.type);
    addToMap(map, "name", this.name);
    addToMap(map, "visible", this.visible, true);
    addToMap(map, "maximumScale", this.maximumScale, 0);
    addToMap(map, "minimumScale", this.minimumScale, Long.MAX_VALUE);
    addToMap(map, "open", this.open, false);
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
