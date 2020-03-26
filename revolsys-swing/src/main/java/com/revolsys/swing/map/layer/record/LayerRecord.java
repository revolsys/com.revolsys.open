package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public interface LayerRecord extends Record {
  default boolean cancelChanges() {
    return true;
  }

  default void clearChanges() {
  }

  default BaseCloseable eventsDisabled() {
    final AbstractRecordLayer layer = getLayer();
    return layer.eventsDisabled();
  }

  default BaseCloseable eventsEnabled() {
    final AbstractRecordLayer layer = getLayer();
    return layer.eventsEnabled();
  }

  default void firePropertyChange(final String fieldName, final Object oldValue,
    final Object newValue) {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      if (layer.isEventsEnabled()) {
        final LayerRecord record = getEventRecord();
        final PropertyChangeEvent fieldEvent = new PropertyChangeEvent(record, fieldName, oldValue,
          newValue);
        layer.propertyChange(fieldEvent);
      }
      fireRecordUpdated();
    }
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
    return layer.getRecordMenu(this);
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
    return layer.getRecordDefinition();
  }

  @SuppressWarnings("unchecked")
  default <R extends LayerRecord> R getRecordProxy() {
    return (R)this;
  }

  default boolean isDeletable() {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isCanDeleteRecords()) {
      return !layer.isDeleted(this);
    }
    return false;
  }

  default boolean isGeometryEditable() {
    return true;
  }

  default boolean isHasModifiedEmptyFields() {
    final List<String> fieldNames = getFieldNames();
    for (final String fieldName : fieldNames) {
      final Object value = getValue(fieldName);
      if (Property.isEmpty(value)) {
        final Object originalValue = getOriginalValue(fieldName);
        if (!Property.isEmpty(originalValue)) {
          return true;
        }
      }
    }
    return false;
  }

  default boolean isHighlighted() {
    final AbstractRecordLayer layer = getLayer();
    return layer.isHighlighted(this);
  }

  default boolean isLayerRecord(final Record record) {
    final AbstractRecordLayer layer = getLayer();
    return layer.isLayerRecord(record);
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

  default boolean isSame(final LayerRecord record) {
    return isSame((Record)record);
  }

  @Override
  default boolean isSame(Record record) {
    if (record == null) {
      return false;
    } else {
      if (record instanceof AbstractProxyLayerRecord) {
        final AbstractProxyLayerRecord proxyRecord = (AbstractProxyLayerRecord)record;
        record = proxyRecord.getRecordProxied();
      }
      if (this == record) {
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
  }

  default boolean isSelected() {
    final AbstractRecordLayer layer = getLayer();
    return layer.isSelected(this);
  }

  @Override
  default boolean isValid(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getFieldIndex(fieldName);
    return isValid(index);
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

  default <R extends Record> void processRecord(final CharSequence title,
    final Consumer<R> action) {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      @SuppressWarnings("unchecked")
      final Set<R> records = Collections.singleton((R)this);
      layer.processTasks(title, records, action, null);
    }
  }

  default LayerRecord revertChanges() {
    return this;
  }

  default void revertEmptyFields() {
    synchronized (this) {
      final AbstractRecordLayer layer = getLayer();
      for (final String fieldName : getFieldNames()) {
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

  default boolean setStateDeleted() {
    setState(RecordState.DELETED);
    return true;
  }
}
