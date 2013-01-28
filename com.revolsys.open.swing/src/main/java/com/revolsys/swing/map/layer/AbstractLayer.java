package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractLayer extends AbstractObjectWithProperties
  implements Layer, PropertyChangeListener {
  private static final AtomicLong ID_GEN = new AtomicLong();

  private boolean editable = false;

  private LayerGroup layerGroup;

  private String name;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private boolean readOnly;

  private boolean selectable = false;

  private boolean selectSupported = true;

  private double maxScale = Double.MAX_VALUE;

  private double minScale = 0;

  private boolean visible = true;

  private boolean queryable;

  private boolean querySupported;

  private final long id = ID_GEN.incrementAndGet();

  private LayerRenderer renderer;

  public AbstractLayer() {
  }

  public AbstractLayer(final String name) {
    this.name = name;
  }

  public AbstractLayer(final String name,
    final Map<String, ? extends Object> properties) {
    this.name = name;
    setProperties(properties);
  }

  @Override
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return new BoundingBox();
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (visible || !visibleLayersOnly) {
      return new BoundingBox();
    } else {
      return getBoundingBox();
    }
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public LayerGroup getLayerGroup() {
    return layerGroup;
  }

  @Override
  public double getMaxScale() {
    return maxScale;
  }

  @Override
  public double getMinScale() {
    return minScale;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPath() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return "/";
    } else {
      final String path = layerGroup.getPath();
      if ("/".equals(path)) {
        return "/" + getName();
      } else {
        return path + "/" + getName();
      }
    }
  }

  @Override
  public Project getProject() {
    if (layerGroup == null) {
      return null;
    } else {
      return layerGroup.getProject();
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  @Override
  public <L extends LayerRenderer<Layer>> L getRenderer() {
    return (L)renderer;
  }

  @Override
  public boolean isEditable() {
    return editable;
  }

  @Override
  public boolean isQueryable() {
    return querySupported && queryable;
  }

  @Override
  public boolean isQuerySupported() {
    return querySupported;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean isSelectable() {
    return selectable;
  }

  @Override
  public boolean isSelectSupported() {
    return selectSupported;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);
  }

  @Override
  public void refresh() {
    // TODO Auto-generated method stub

  }

  @Override
  public void remove() {
    if (layerGroup != null) {
      layerGroup.remove(this);
      layerGroup = null;
    }
  }

  @Override
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public void setEditable(final boolean editable) {
    if (this.editable != editable) {
      final boolean old = this.editable;
      this.editable = editable;
      propertyChangeSupport.firePropertyChange("editable", old, editable);
    }
  }

  @Override
  public void setLayerGroup(final LayerGroup layerGroup) {
    if (this.layerGroup != layerGroup) {
      final LayerGroup old = this.layerGroup;
      this.layerGroup = layerGroup;
      propertyChangeSupport.firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaxScale(final double maxScale) {
    this.maxScale = maxScale;
  }

  @Override
  public void setMinScale(final double minScale) {
    this.minScale = minScale;
  }

  @Override
  public void setName(final String name) {
    if (!EqualsRegistry.equal(name, this.name)) {
      propertyChangeSupport.firePropertyChange("name", this.name, name);
      this.name = name;
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties == null || !this.getProperties().equals(properties)) {
      propertyChangeSupport.firePropertyChange("properties",
        this.getProperties(), properties);
      super.setProperties(properties);
      JavaBeanUtil.setProperties(this, properties);
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    final Object oldValue = getProperty(name);
    if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
      final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(this,
        "property", oldValue, value, name);
      propertyChangeSupport.firePropertyChange(event);

      super.setProperty(name, value);
      JavaBeanUtil.setProperty(this, name, value);

    }
  }

  @Override
  public void setQueryable(final boolean queryable) {
    this.queryable = queryable;
  }

  protected void setQuerySupported(final boolean querySupported) {
    this.querySupported = querySupported;
  }

  @Override
  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  protected void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    this.renderer = renderer;
  }

  @Override
  public void setSelectable(final boolean selectable) {
    this.selectable = selectable;
  }

  public void setSelectSupported(final boolean selectSupported) {
    this.selectSupported = selectSupported;
  }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    propertyChangeSupport.firePropertyChange("visible", oldVisible, visible);
  }

  @Override
  public String toString() {
    return getName();
  }
}
