package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;

import com.revolsys.datatype.DataType;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.revolsys.util.enableable.BooleanValueCloseable;

public interface LayerRecord extends Record {
  default <V extends LayerRecord> int addTo(final List<V> records) {
    if (!contains(records)) {
      final int index = records.size();
      ((List)records).add(this);
      return index;
    }
    return -1;
  }

  default boolean cancelChanges() {
    return true;
  }

  default void clearChanges() {
  }

  default <V extends LayerRecord> boolean contains(final Iterable<V> records) {
    for (final V record : records) {
      if (isSame(record)) {
        return true;
      }
    }
    return false;
  }

  default BooleanValueCloseable eventsDisabled() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.eventsDisabled();
    }
  }

  default BooleanValueCloseable eventsEnabled() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.eventsEnabled();
    }
  }

  default void firePropertyChange(final String fieldName, final Object oldValue,
    final Object newValue) {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final LayerRecord record = getEventRecord();
      final PropertyChangeEvent fieldEvent = new PropertyChangeEvent(record, fieldName, oldValue,
        newValue);
      layer.propertyChange(fieldEvent);
    }
    fireRecordUpdated();
  }

  default void fireRecordUpdated() {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final LayerRecord record = getEventRecord();
      final PropertyChangeEvent fieldEvent = new PropertyChangeEvent(layer,
        AbstractRecordLayer.RECORD_UPDATED, null, record);
      layer.propertyChange(fieldEvent);
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

  default LayerRecordMenu getMenu() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.getRecordMenu(this);
    }
  }

  default Record getOriginalRecord() {
    return new OriginalRecord(this);
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

  default <V extends LayerRecord> int indexOf(final Iterable<V> records) {
    int index = 0;
    for (final V record : records) {
      if (isSame(record)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  default boolean isDeletable() {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isCanDeleteRecords()) {
      return !isDeleted();
    }
    return false;
  }

  default boolean isGeometryEditable() {
    return true;
  }

  default boolean isLayerRecord(final Record record) {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return false;
    } else {
      return layer.isLayerRecord(record);
    }
  }

  default boolean isModified(final int index) {
    final String fieldName = getFieldName(index);
    return isModified(fieldName);
  }

  default boolean isModified(final String name) {
    return false;
  }

  default boolean isProxyRecord() {
    return false;
  }

  default boolean isSame(final Record record) {
    if (record == null) {
      return false;
    } else if (this == record) {
      return true;
    } else {
      synchronized (this) {
        if (isLayerRecord(record)) {
          final Identifier id = getIdentifier();
          final Identifier otherId = record.getIdentifier();
          if (id == null || otherId == null) {
            return false;
          } else if (DataType.equal(id, otherId)) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    }
  }

  @Override
  default boolean isValid(final int index) {
    synchronized (this) {
      if (getState() == RecordState.INITIALIZING) {
        return true;
      } else {
        final FieldDefinition fieldDefinition = getFieldDefinition(index);
        if (fieldDefinition != null) {
          final Object value = getValue(index);
          return fieldDefinition.isValid(value);
        }
        return true;
      }
    }
  }

  @Override
  default boolean isValid(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getFieldIndex(fieldName);
    return isValid(index);
  }

  default LayerRecord newRecordProxy() {
    return this;
  }

  default void postSaveDeleted() {
  }

  default void postSaveModified() {
    synchronized (this) {
      if (getState() == RecordState.PERSISTED) {
        clearChanges();
      }
    }
  }

  default void postSaveNew() {
  }

  /**
   * Remove the first record in the collection of records that is {{@link #isSame(Record)}} as this record.
   *
   * @param records
   * @return The index of the removed record.
   */
  default int removeFrom(final Iterable<? extends LayerRecord> records) {
    int index = 0;
    for (final Iterator<? extends LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
      final LayerRecord record = iterator.next();
      if (record.isSame(this)) {
        iterator.remove();
        return index;
      } else {
        index++;
      }
    }
    return -1;
  }

  default LayerRecord revertChanges() {
    return this;
  }

  default void revertEmptyFields() {
    synchronized (this) {
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
  }

  default void validate() {
  }
}
