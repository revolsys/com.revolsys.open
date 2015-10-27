package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;

import com.revolsys.equals.Equals;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.util.Property;
import com.revolsys.util.enableable.Enabled;

public interface LayerRecord extends Record {
  default void cancelChanges() {
  }

  default void clearChanges() {
  }

  default Enabled eventsDisabled() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.eventsDisabled();
    }
  }

  default Enabled eventsEnabled() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.eventsEnabled();
    }
  }

  default void firePropertyChange(final String fieldName, final Object oldValue,
    final Object newValue) {
    final AbstractLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final LayerRecord record = getEventRecord();
      final PropertyChangeEvent event = new PropertyChangeEvent(record, fieldName, oldValue,
        newValue);
      layer.propertyChange(event);
    }
  }

  /**
   * Get the record to use for property change events.
   *
   * @return The record;
   */
  default LayerRecord getEventRecord() {
    return this;
  }

  AbstractRecordLayer getLayer();

  default LayerRecord getOriginalRecord() {
    return new LayerRecord() {
      @Override
      public LayerRecord clone() {
        return this;
      }

      @Override
      public AbstractRecordLayer getLayer() {
        return LayerRecord.this.getLayer();
      }

      @Override
      public RecordDefinition getRecordDefinition() {
        return LayerRecord.this.getRecordDefinition();
      }

      @Override
      public <T> T getValue(final int index) {
        final String fieldName = getFieldName(index);
        return LayerRecord.this.getOriginalValue(fieldName);
      }

      @Override
      public void setState(final RecordState state) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean setValue(final int index, final Object value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @SuppressWarnings("unchecked")
  default <T> T getOriginalValue(final String name) {
    return (T)getValue(name);
  }

  @Override
  default RecordDefinition getRecordDefinition() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.getRecordDefinition();
    }
  }

  default boolean isDeletable() {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isCanDeleteRecords()) {
      return !isDeleted();
    }
    return false;
  }

  default boolean isDeleted() {
    return getState() == RecordState.Deleted;
  }

  default boolean isGeometryEditable() {
    return true;
  }

  default boolean isModified(final int index) {
    final String fieldName = getFieldName(index);
    return isModified(fieldName);
  }

  default boolean isModified(final String name) {
    return false;
  }

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

  @Override
  default boolean isValid(final int index) {
    if (getState() == RecordState.Initializing) {
      return true;
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String name = recordDefinition.getFieldName(index);
      return isValid(name);
    }
  }

  @Override
  default boolean isValid(final String name) {
    if (getState() == RecordState.Initializing) {
      return true;
    } else {
      final FieldDefinition fieldDefinition = getFieldDefinition(name);
      if (fieldDefinition != null && fieldDefinition.isRequired()) {
        final Object value = getValue(name);
        return fieldDefinition.isValid(value);
      }
      return true;
    }
  }

  default void postSaveDeleted() {
  }

  default void postSaveModified() {
    if (getState() == RecordState.Persisted) {
      clearChanges();
    }
  }

  default void postSaveNew() {
  }

  default LayerRecord revertChanges() {
    return this;
  }

  default void revertEmptyFields() {
    final AbstractRecordLayer layer = getLayer();
    for (final String fieldName : getRecordDefinition().getFieldNames()) {
      final Object value = getValue(fieldName);
      if (Property.isEmpty(value)) {
        if (!layer.isFieldUserReadOnly(fieldName)) {
          final Object originalValue = getOriginalValue(fieldName);
          if (!Property.isEmpty(originalValue)) {
            setValue(fieldName, originalValue);
          }
        }
      }
    }
  }

  default void validate() {
  }
}
