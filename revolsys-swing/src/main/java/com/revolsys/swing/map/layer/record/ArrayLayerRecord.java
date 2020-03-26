package com.revolsys.swing.map.layer.record;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class ArrayLayerRecord extends ArrayRecord implements LayerRecord {
  private static final Map<String, Object> EMPTY_ORIGINAL_VALUES = Collections.emptyMap();

  private Identifier identifier;

  protected final AbstractRecordLayer layer;

  protected Map<String, Object> originalValues = EMPTY_ORIGINAL_VALUES;

  public ArrayLayerRecord(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
  }

  public ArrayLayerRecord(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    super(layer.getRecordDefinition());
    setState(RecordState.INITIALIZING);
    initValues(values);
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
  public final synchronized boolean cancelChanges() {
    boolean cancelled = false;
    final Map<String, Object> originalValues = this.originalValues;
    RecordState state = getState();
    final AbstractRecordLayer layer = getLayer();
    try (
      BaseCloseable disabled = layer.eventsDisabled()) {
      if (originalValues != EMPTY_ORIGINAL_VALUES) {
        setState(RecordState.INITIALIZING);
        super.setValues(originalValues);
      }
      if (state == RecordState.MODIFIED || state == RecordState.DELETED) {
        state = RecordState.PERSISTED;
        cancelled = true;
      }
    } finally {
      setState(state);
    }
    this.originalValues = EMPTY_ORIGINAL_VALUES;
    if (cancelled) {
      firePropertyChange(EVENT_RECORD_CHANGED, false, true);
    }
    return cancelled;
  }

  @Override
  public synchronized void clearChanges() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      this.originalValues = EMPTY_ORIGINAL_VALUES;
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
  public synchronized <T> T getOriginalValue(final String name) {
    final Map<String, Object> originalValues = this.originalValues;
    if (originalValues.containsKey(name)) {
      return (T)originalValues.get(name);
    }
    return (T)getValue(name);
  }

  @Override
  public boolean isModified(final String fieldName) {
    final Map<String, Object> originalValues = this.originalValues;
    if (originalValues == null) {
      return false;
    } else {
      return originalValues.containsKey(fieldName);
    }
  }

  protected Map<String, Object> removeOriginalValue(final Map<String, Object> originalValues,
    final String fieldName) {
    if (!originalValues.isEmpty()) {
      originalValues.remove(fieldName);
      if (originalValues.isEmpty()) {
        this.originalValues = EMPTY_ORIGINAL_VALUES;
      }
    }
    return this.originalValues;
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
  protected synchronized boolean setValue(final FieldDefinition fieldDefinition,
    final Object value) {
    boolean updated = false;
    final int fieldIndex = fieldDefinition.getIndex();
    final String fieldName = fieldDefinition.getName();

    final Object newValue = fieldDefinition.toFieldValue(value);
    final Object oldValue = getValue(fieldIndex);
    RecordState newState = null;
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
            Map<String, Object> originalValues = this.originalValues;
            if (fieldDefinition.equals(originalValue, newValue)) {
              if (originalValues != EMPTY_ORIGINAL_VALUES) {
                originalValues = removeOriginalValue(originalValues, fieldName);
                if (originalValues.isEmpty()) {
                  newState = RecordState.PERSISTED;
                }
              }
            } else {
              if (originalValues == EMPTY_ORIGINAL_VALUES) {
                originalValues = new HashMap<>();
              }
              originalValues.put(fieldName, originalValue);
              if (RecordState.INITIALIZING != state) {
                newState = RecordState.MODIFIED;
              }
            }
            this.originalValues = originalValues;
          } else {
            throw new IllegalStateException("Editing records is not supported for layer " + layer);
          }
        break;
      }
      updated |= super.setValue(fieldDefinition, newValue);
      if (newState != null) {
        setState(newState);
      }
      if (state != RecordState.INITIALIZING) {
        firePropertyChange(fieldName, oldValue, newValue);
        layer.updateRecordState(this);
      }
    }
    return updated;
  }
}
