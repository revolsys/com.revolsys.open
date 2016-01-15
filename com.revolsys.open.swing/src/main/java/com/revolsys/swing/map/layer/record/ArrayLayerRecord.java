package com.revolsys.swing.map.layer.record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.ValueCloseable;

public class ArrayLayerRecord extends ArrayRecord implements LayerRecord {
  private static final long serialVersionUID = 1L;

  public static ArrayLayerRecord newRecordNew(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    final ArrayLayerRecord record = new ArrayLayerRecord(layer);
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    if (values != null) {
      record.setState(RecordState.INITIALIZING);
      final List<FieldDefinition> idFields = recordDefinition.getIdFields();
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (!idFields.contains(fieldDefinition)) {
          final String fieldName = fieldDefinition.getName();
          final Object value = values.get(fieldName);
          fieldDefinition.setValue(record, value);
        }
      }
      record.setState(RecordState.NEW);
    }
    return record;
  }

  public static ArrayLayerRecord newRecordPersisted(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    final ArrayLayerRecord record = new ArrayLayerRecord(layer);
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    record.setState(RecordState.INITIALIZING);
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      final String fieldName = fieldDefinition.getName();
      final Object value = values.get(fieldName);
      fieldDefinition.setValue(record, value);
    }
    record.setState(RecordState.PERSISTED);
    return record;
  }

  private Identifier identifier;

  private final AbstractRecordLayer layer;

  private Map<String, Object> originalValues;

  public ArrayLayerRecord(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
  }

  public ArrayLayerRecord(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    super(layer.getRecordDefinition());
    setState(RecordState.INITIALIZING);
    setValues(values);
    setState(RecordState.PERSISTED);
    this.layer = layer;
  }

  protected ArrayLayerRecord(final RecordStoreLayer layer,
    final RecordDefinition recordDefinition) {
    super(recordDefinition);
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public final boolean cancelChanges() {
    boolean cancelled = false;
    synchronized (this) {
      RecordState state = getState();
      try (
        ValueCloseable<?>  disabled = getLayer().eventsDisabled()) {
        if (this.originalValues != null) {
          setState(RecordState.INITIALIZING);
          super.setValues(this.originalValues);
          this.originalValues = null;
        }
        if (state == RecordState.MODIFIED || state == RecordState.DELETED) {
          state = RecordState.PERSISTED;
          cancelled = true;
        }
      } finally {
        setState(state);
      }
    }
    if (cancelled) {
      firePropertyChange(EVENT_RECORD_CHANGED, false, true);
    }
    return cancelled;
  }

  @Override
  public synchronized void clearChanges() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      this.originalValues = null;
    }
  }

  @Override
  public Identifier getIdentifier() {
    if (this.identifier == null) {
      this.identifier = super.getIdentifier();
    }
    return this.identifier;
  }

  @Override
  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
    synchronized (this) {
      if (this.originalValues != null) {
        if (this.originalValues.containsKey(name)) {
          return (T)this.originalValues.get(name);
        }
      }
    }
    return (T)getValue(name);
  }

  protected Object getSync() {
    return getLayer().getSync();
  }

  @Override
  public synchronized boolean isModified(final String name) {
    if (this.originalValues == null) {
      return false;
    } else {
      return this.originalValues.containsKey(name);
    }
  }

  @Override
  public LayerRecord revertChanges() {
    if (cancelChanges()) {
      final AbstractRecordLayer layer = getLayer();
      layer.revertChanges(this);
      firePropertyChange("state", RecordState.MODIFIED, RecordState.PERSISTED);
    }
    return this;
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    boolean updated = false;
    final int fieldIndex = fieldDefinition.getIndex();
    final String fieldName = fieldDefinition.getName();

    final Object newValue = fieldDefinition.toFieldValue(value);
    final Object oldValue = getValue(fieldIndex);
    if (!DataType.equal(oldValue, newValue)) {
      final AbstractRecordLayer layer = getLayer();
      final RecordState state = getState();
      switch (state) {
        case INITIALIZING:
        // Allow modification on initialization
        break;
        case NEW:
          if (!layer.isCanAddRecords()) {
            throw new IllegalStateException(
              "Adding new records is not supported for layer " + layer);
          }
        break;
        case DELETED:
          throw new IllegalStateException("Cannot edit a deleted record for layer " + layer);
        case PERSISTED:
        case MODIFIED:
          if (layer.isCanEditRecords()) {
            final Object originalValue = getOriginalValue(fieldName);
            synchronized (this) {
              if (fieldDefinition.equals(originalValue, newValue)) {
                if (this.originalValues != null) {
                  this.originalValues.remove(fieldName);
                  if (this.originalValues.isEmpty()) {
                    this.originalValues = null;
                    setState(RecordState.PERSISTED);
                  }
                }
              } else {
                if (this.originalValues == null) {
                  this.originalValues = new HashMap<>();
                }
                this.originalValues.put(fieldName, originalValue);
                if (RecordState.INITIALIZING != state) {
                  setState(RecordState.MODIFIED);
                }
              }
            }
          } else {
            throw new IllegalStateException("Editing records is not supported for layer " + layer);
          }
        break;
      }
      updated |= super.setValue(fieldDefinition, newValue);
      if (state != RecordState.INITIALIZING) {
        firePropertyChange(fieldName, oldValue, newValue);
        layer.updateRecordState(this);
      }
    }
    return updated;
  }
}
