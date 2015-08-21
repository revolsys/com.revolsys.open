package com.revolsys.swing.map.layer.record;

import com.revolsys.data.equals.Equals;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.Record;

public interface LayerRecord extends Record {
  void cancelChanges();

  void clearChanges();

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  AbstractRecordLayer getLayer();

  <T> T getOriginalValue(String fieldName);

  boolean isDeletable();

  boolean isDeleted();

  boolean isGeometryEditable();

  boolean isModified(int attributeIndex);

  boolean isModified(String name);

  default boolean isSame(final Record record) {
    if (record == null) {
      return false;
    } else if (this == record) {
      return true;
    } else {
      final AbstractRecordLayer layer = getLayer();
      if (layer.isLayerRecord(record)) {
        final Identifier id = getIdentifier();
        final Identifier otherId = record.getIdentifier();
        if (id == null || otherId == null) {
          return false;
        } else if (Equals.equal(id, otherId)) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  void postSaveDeleted();

  void postSaveModified();

  void postSaveNew();

  LayerRecord revertChanges();

  void revertEmptyFields();

  void validate();
}
