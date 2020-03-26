package com.revolsys.swing.map.layer.record;

import java.util.List;
import java.util.Map;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public abstract class RecordStoreLayerRecord extends ArrayLayerRecord {

  public RecordStoreLayerRecord(final RecordStoreLayer layer) {
    super(layer);
  }

  protected RecordStoreLayerRecord(final RecordStoreLayer layer,
    final RecordDefinition recordDefinition) {
    super(layer, recordDefinition);
  }

  @Override
  public LayerRecord getEventRecord() {
    return getRecordProxy();
  }

  @Override
  public RecordStoreLayer getLayer() {
    return (RecordStoreLayer)super.getLayer();
  }

  public synchronized void refreshFromRecordStore(final Record record) {
    final RecordState oldState = super.setState(RecordState.INITIALIZING);
    RecordState newState = null;
    try {
      if (record != null) {
        Map<String, Object> originalValues = this.originalValues;
        final List<FieldDefinition> fields = getFieldDefinitions();
        for (final FieldDefinition fieldDefinition : fields) {
          if (!isIdField(fieldDefinition)) {
            final String fieldName = fieldDefinition.getName();
            if (record.hasField(fieldName)) {
              final int fieldIndex = fieldDefinition.getIndex();
              final Object recordValue = record.getValue(fieldName);
              final Object newValue = fieldDefinition.toFieldValue(recordValue);
              final Object value = getValueInternal(fieldIndex);
              if (originalValues.containsKey(fieldName)) {
                if (fieldDefinition.equals(value, newValue)) {
                  originalValues = removeOriginalValue(originalValues, fieldName);
                  if (originalValues.isEmpty() && oldState.isModified()) {
                    newState = RecordState.PERSISTED;
                  }
                } else {
                  originalValues.put(fieldName, newValue);
                }
              } else if (!fieldDefinition.equals(value, newValue)) {
                setValueInternal(fieldIndex, newValue);
              }
            }
          }
        }
      }
    } finally {
      if (newState == null) {
        setState(oldState);
      } else {
        setState(newState);
        this.layer.updateRecordState(this);
      }
    }
  }

}
