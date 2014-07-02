package com.revolsys.swing.map.layer.record;

import com.revolsys.gis.data.model.DataObject;

public interface LayerRecord extends DataObject {
  void cancelChanges();

  void clearChanges();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  AbstractDataObjectLayer getLayer();

  <T> T getOriginalValue(String attributeName);

  boolean isDeletable();

  boolean isDeleted();

  boolean isGeometryEditable();

  boolean isModified(int attributeIndex);

  boolean isModified(String name);

  boolean isSame(LayerRecord record);

  LayerRecord revertChanges();

  void revertEmptyFields();
}
