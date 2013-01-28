package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.io.ObjectWithProperties;

public interface Layer extends PropertyChangeSupportProxy, ObjectWithProperties {
  void addPropertyChangeListener(final PropertyChangeListener listener);

  void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener);

  BoundingBox getBoundingBox();

  BoundingBox getBoundingBox(boolean visibleLayersOnly);

  long getId();

  LayerGroup getLayerGroup();

  double getMaxScale();

  double getMinScale();

  String getName();

  Project getProject();

  <L extends LayerRenderer<Layer>> L getRenderer();

  boolean isEditable();

  boolean isQueryable();

  boolean isQuerySupported();

  boolean isReadOnly();

  boolean isSelectable();

  boolean isSelectSupported();

  boolean isVisible();

  void refresh();

  void remove();

  void removePropertyChangeListener(final PropertyChangeListener listener);

  void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener);

  void setEditable(boolean editable);

  void setLayerGroup(LayerGroup layerGroup);

  void setMaxScale(double maxScale);

  void setMinScale(double minScale);

  void setName(String name);

  void setQueryable(boolean b);

  void setReadOnly(boolean readOnly);

  void setSelectable(boolean selectable);

  void setVisible(boolean visible);
}
