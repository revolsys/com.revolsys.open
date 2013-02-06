package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.CaseConverter;

public abstract class AbstractLayerRenderer<T extends Layer> implements
  LayerRenderer<T>, PropertyChangeListener {

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }
  private final T layer;

  private double minimumScale = 0;

  private double maximumScale = Double.MAX_VALUE;

  private boolean visible = true;

  private final String type;

  private String name;

  private LayerRenderer<?> parent;

  private Map<String, Object> defaults = new HashMap<String, Object>();

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }
  
  public AbstractLayerRenderer(final String type, final T layer) {
    this.type = type;
    this.layer = layer;
    propertyChangeSupport.addPropertyChangeListener(layer);
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
    final Map<String, Object> styleDefaults = (Map<String, Object>)style.get("defaults");
    setDefaults(styleDefaults);
    final Number minimumScale = getValue(style, "minimumScale");
    if (minimumScale != null) {
      this.minimumScale = minimumScale.doubleValue();
    }
    final Number maximumScale = getValue(style, "maximumScale");
    if (maximumScale != null) {
      this.maximumScale = maximumScale.doubleValue();
    }
    final Boolean visible = getValue(style, "visible");
    if (visible != null) {
      this.visible = visible;
    }
    setName((String)style.get("name"));
  }

  @Override
  public Map<String, Object> getAllDefaults() {

    if (parent == null) {
      return new LinkedHashMap<String, Object>(defaults);
    } else {
      final Map<String, Object> allDefaults = parent.getAllDefaults();
      allDefaults.putAll(getDefaults());
      return allDefaults;
    }
  }

  public Map<String, Object> getDefaults() {
    return defaults;
  }

  public T getLayer() {
    return layer;
  }

  public double getMaximumScale() {
    return maximumScale;
  }

  public double getMinimumScale() {
    return minimumScale;
  }

  @Override
  public String getName() {
    return name;
  }

  public LayerRenderer<?> getParent() {
    return parent;
  }

  public String getType() {
    return type;
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
    Object value = defaults.get(name);
    if (value == null && parent != null) {
      value = parent.getValue(name);
    }
    return (V)value;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  public boolean isVisible(final double scale) {
    if (isVisible()) {
      if (scale >= minimumScale) {
        if (scale <= maximumScale) {
          return true;
        }
      }
    }
    return false;
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

  public void setMaximumScale(final double maximumScale) {
    this.maximumScale = maximumScale;
  }

  public void setMinimumScale(final double minimumScale) {
    this.minimumScale = minimumScale;
  }

  public void setName(final String name) {
    String oldName = getName();
    if (StringUtils.hasText(name)) {
      this.name = name;
    } else {
      this.name = CaseConverter.toCapitalizedWords(type);
    }
    propertyChangeSupport.firePropertyChange("name", oldName, this.name);
      }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    propertyChangeSupport.firePropertyChange("visible", oldVisible, visible);
  }
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", type);
    if (StringUtils.hasText(name)) {
      map.put("name", name);
    }
    if (!defaults.isEmpty()) {
      map.put("defaults", defaults);
    }
    map.put("minimumScale", minimumScale);
    map.put("maximumScale", maximumScale);
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    if (StringUtils.hasText(name)) {
      return name;
    } else {
      return type;
    }
  }

}
