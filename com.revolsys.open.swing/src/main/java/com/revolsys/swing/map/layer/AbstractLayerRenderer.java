package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.panel.BaseStylePanel;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractLayerRenderer<T extends Layer> implements
  LayerRenderer<T>, PropertyChangeListener, Cloneable {

  private static final Icon ICON = SilkIconLoader.getIcon("palette");

  private Map<String, Object> defaults = new HashMap<String, Object>();

  private Icon icon = ICON;

  private final T layer;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private String name;

  private LayerRenderer<?> parent;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final String type;

  private boolean visible = true;

  public AbstractLayerRenderer(final String type, final T layer) {
    this.type = type;
    this.layer = layer;
    this.propertyChangeSupport.addPropertyChangeListener(layer);
    this.name = CaseConverter.toCapitalizedWords(type);
  }

  public AbstractLayerRenderer(final String type, final T layer,
    final LayerRenderer<?> parent) {
    this(type, layer, parent, Collections.<String, Object> emptyMap());
  }

  public AbstractLayerRenderer(final String type, final T layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    this(type, layer);
    this.parent = parent;
    getPropertyChangeSupport().addPropertyChangeListener(parent);
    @SuppressWarnings("unchecked")
    final Map<String, Object> styleDefaults = (Map<String, Object>)style.remove("defaults");
    setDefaults(styleDefaults);
    final Number minimumScale = getValue(style, "minimumScale");
    if (minimumScale != null) {
      this.minimumScale = minimumScale.longValue();
    }
    final Number maximumScale = getValue(style, "maximumScale");
    if (maximumScale != null) {
      this.maximumScale = maximumScale.longValue();
    }
    final Boolean visible = getValue(style, "visible");
    if (visible != null) {
      this.visible = visible;
    }
    setName((String)style.remove("name"));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected AbstractLayerRenderer<T> clone() {
    try {
      final AbstractLayerRenderer<T> clone = (AbstractLayerRenderer<T>)super.clone();
      clone.defaults = JavaBeanUtil.clone(this.defaults);
      return clone;
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public ValueField createStylePanel() {
    return new BaseStylePanel(this);
  }

  @Override
  public Map<String, Object> getAllDefaults() {
    final Map<String, Object> allDefaults;
    if (this.parent == null) {
      allDefaults = new LinkedHashMap<String, Object>(getDefaults());
    } else {
      allDefaults = this.parent.getAllDefaults();
      allDefaults.putAll(getDefaults());
    }
    return allDefaults;
  }

  public Map<String, Object> getDefaults() {
    return this.defaults;
  }

  @Override
  public Icon getIcon() {
    return this.icon;
  }

  public T getLayer() {
    return this.layer;
  }

  public double getMaximumScale() {
    return this.maximumScale;
  }

  public double getMinimumScale() {
    return this.minimumScale;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public LayerRenderer<?> getParent() {
    return this.parent;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public String getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  protected <V> V getValue(final Map<String, Object> map, final String name) {
    Object value = map.get(name);
    if (value == null) {
      value = getValue(name);
    }
    return (V)value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final String name) {
    Object value = this.defaults.get(name);
    if (value == null && this.parent != null) {
      value = this.parent.getValue(name);
    }
    return (V)value;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  public boolean isVisible(final double scale) {
    if (isVisible()) {
      final long longScale = (long)scale;
      if (getMinimumScale() >= longScale && longScale >= getMaximumScale()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  @Override
  public final void render(final Viewport2D viewport, final Graphics2D graphics) {
    final T layer = getLayer();
    final double scale = viewport.getScale();
    if (isVisible(scale)) {
      render(viewport, graphics, layer);
    }
  }

  public abstract void render(Viewport2D viewport, Graphics2D graphics, T layer);

  public void setDefaults(final Map<String, Object> defaults) {
    if (defaults == null) {
      this.defaults.clear();
    } else {
      this.defaults = new LinkedHashMap<String, Object>(defaults);
    }
  }

  public void setIcon(final Icon icon) {
    this.icon = icon;
  }

  public void setMaximumScale(final long maximumScale) {
    this.maximumScale = maximumScale;
  }

  public void setMinimumScale(final long minimumScale) {
    this.minimumScale = minimumScale;
  }

  public void setName(final String name) {
    final String oldName = getName();
    if (StringUtils.hasText(name)) {
      this.name = name;
    } else {
      this.name = CaseConverter.toCapitalizedWords(this.type);
    }
    this.propertyChangeSupport.firePropertyChange("name", oldName, this.name);
  }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    this.propertyChangeSupport.firePropertyChange("visible", oldVisible,
      visible);
  }

  @Override
  public Map<String, Object> toMap() {
    return toMap(Collections.<String, Object> emptyMap());
  }

  public Map<String, Object> toMap(final Map<String, Object> defaults) {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    MapSerializerUtil.add(map, "type", this.type);
    MapSerializerUtil.add(map, "name", this.name);
    MapSerializerUtil.add(map, "visible", this.visible, true);
    final Map<String, Object> newDefaults = new LinkedHashMap<String, Object>(
      this.defaults);
    for (final Entry<String, Object> entry : defaults.entrySet()) {
      final String name = entry.getKey();
      boolean defaultEqual = false;
      if (newDefaults.containsKey(name)) {
        final Object defaultValue = entry.getValue();
        final Object newDefaultValue = newDefaults.get(name);
        defaultEqual = EqualsRegistry.equal(defaultValue, newDefaultValue);
      }
      if (defaultEqual) {
        newDefaults.remove(name);
      }
    }
    if (!newDefaults.isEmpty()) {
      map.put("defaults", newDefaults);
    }
    MapSerializerUtil.add(map, "maximumScale", this.maximumScale, 0);
    MapSerializerUtil.add(map, "minimumScale", this.minimumScale,
      Long.MAX_VALUE);
    return map;
  }

  @Override
  public String toString() {
    if (StringUtils.hasText(this.name)) {
      return this.name;
    } else {
      return this.type;
    }
  }

}
