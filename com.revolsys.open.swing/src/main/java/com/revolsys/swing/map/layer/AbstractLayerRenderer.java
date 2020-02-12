package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;
import com.revolsys.swing.map.view.ViewRenderer;
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

  public AbstractLayerRenderer(final String type, final String name, final Icon icon) {
    this.type = type;
    setName(name);
    setIcon(icon);
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

  @Override
  public T getLayer() {
    return this.layer;
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

  public boolean isHasParent() {
    return getParent() != null;
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
  public boolean isVisible(final ViewRenderer view) {
    final double scaleForVisible = view.getScaleForVisible();
    return isVisible(scaleForVisible);
  }

  @Override
  public Form newStylePanel() {
    return new BaseStylePanel(this, true);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    firePropertyChange(event);
  }

  @Override
  public final void render(final ViewRenderer view) {
    final T layer = getLayer();
    if (layer != null) {
      if (isVisible(view)) {
        render(view, layer);
      }
    }
  }

  public abstract void render(ViewRenderer view, T layer);

  @Override
  public void setEditing(final boolean editing) {
    final boolean oldValue = this.editing;
    this.editing = editing;
    firePropertyChange("editing", oldValue, this.editing);
  }

  public void setIcon(final Icon icon) {
    final Object oldValue = this.icon;
    if (icon == null) {
      this.icon = ICON;
    } else {
      this.icon = icon;
    }
    firePropertyChange("icon", oldValue, this.icon);
  }

  public void setIcon(final String iconName) {
    final Icon icon = Icons.getIcon(iconName);
    setIcon(icon);
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

  @Override
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
    if (this.open != open) {
      final boolean oldValue = this.open;
      this.open = open;
      firePropertyChange("open", oldValue, this.open);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setParent(final LayerRenderer<?> parent) {
    final LayerRenderer<?> oldValue = this.parent;
    Property.removeListener(this, oldValue);
    this.parent = parent;
    if (parent == null) {
      setLayer(null);
    } else {
      final T layer = (T)parent.getLayer();
      setLayer(layer);
      Property.addListener(this, parent);
    }
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
  public JsonObject toMap() {
    final JsonObject map = newMapTree(this.type);
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
