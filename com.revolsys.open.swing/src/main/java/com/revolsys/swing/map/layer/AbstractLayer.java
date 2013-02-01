package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.LoggerFactory;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
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

  private GeometryFactory geometryFactory;

  private boolean readOnly;

  private boolean selectable = false;

  private boolean selectSupported = true;

  private double maximumScale = 0;

  private double minimumScale = Double.MAX_VALUE;

  private boolean visible = true;

  private boolean queryable;

  private boolean querySupported;

  private final long id = ID_GEN.incrementAndGet();

  private LayerRenderer<?> renderer;

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
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
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
  public double getMaximumScale() {
    return maximumScale;
  }

  @Override
  public double getMinimumScale() {
    return minimumScale;
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

  @SuppressWarnings("unchecked")
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
  public boolean isVisible(final double scale) {
    if (isVisible()) {
      if ((long)scale <= getMinimumScale()) {
        if ((long)scale >= getMaximumScale()) {
          return true;
        }
      }
    }
    return false;
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

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
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
  public void setMaximumScale(final double maxScale) {
    this.maximumScale = maxScale;
  }

  @Override
  public void setMinimumScale(final double minScale) {
    this.minimumScale = minScale;
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
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    //TODO see if we can get the JavaBeanUtil set property to work with conversions
    if (name.equals("minimumScale")) {
      setMinimumScale(((Number)value).doubleValue());
    } else if (name.equals("maximumScale")) {
      setMaximumScale(((Number)value).doubleValue());
    } else {
      final Object oldValue = getProperty(name);
      if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
        final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(
          this, "property", oldValue, value, name);
        propertyChangeSupport.firePropertyChange(event);

        super.setProperty(name, value);
        try {
        JavaBeanUtil.setProperty(this , name, value);
        } catch (Throwable e) {
          LoggerFactory.getLogger(getClass()).error("Unable to set property:" + name,e);
        }
      }
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
