package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.Child;
import com.revolsys.io.ObjectWithProperties;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.component.TabbedValuePanel;

public interface Layer extends PropertyChangeSupportProxy,
ObjectWithProperties, PropertyChangeListener, Comparable<Layer>,
MapSerializer, Child<LayerGroup>, Cloneable {

  TabbedValuePanel createPropertiesPanel();

  void delete();

  BoundingBox getBoundingBox();

  BoundingBox getBoundingBox(boolean visibleLayersOnly);

  Collection<Class<?>> getChildClasses();

  GeometryFactory getGeometryFactory();

  long getId();

  LayerGroup getLayerGroup();

  long getMaximumScale();

  long getMinimumScale();

  String getName();

  String getPath();

  List<Layer> getPathList();

  Project getProject();

  <L extends LayerRenderer<? extends Layer>> L getRenderer();

  BoundingBox getSelectedBoundingBox();

  String getType();

  void initialize();

  boolean isClonable();

  boolean isEditable();

  boolean isEditable(double scale);

  boolean isExists();

  boolean isHasChanges();

  boolean isHasGeometry();

  boolean isInitialized();

  boolean isQueryable();

  boolean isQuerySupported();

  boolean isReadOnly();

  boolean isSelectable();

  boolean isSelectable(double scale);

  boolean isSelectSupported();

  boolean isVisible();

  boolean isVisible(double scale);

  void refresh();

  boolean saveChanges();

  boolean saveSettings(File directory);

  void setEditable(boolean editable);

  void setLayerGroup(LayerGroup layerGroup);

  /**
   * Set the maximum scale. This is the scale that if you zoom in to a more
   * detailed scale than the maximum scale the layer will not be visible. This
   * is inverse from the logical definition of maximum. If scale < maximumScale
   * it will not be shown.
   */
  void setMaximumScale(long maximumScale);

  /**
   * Set the minimum scale. This is the scale that if you zoom out to a less
   * detailed scale than the minimum scale the layer will not be visible. This
   * is inverse from the logical definition of minimum. If scale > minimumScale
   * it will not be shown.
   */
  void setMinimumScale(long minimumScale);

  void setName(String name);

  void setQueryable(boolean b);

  void setReadOnly(boolean readOnly);

  void setRenderer(final LayerRenderer<? extends Layer> renderer);

  void setSelectable(boolean selectable);

  void setVisible(boolean visible);

  void showProperties();

  void showProperties(String tabName);

  void showRendererProperties(final LayerRenderer<?> renderer);
}
