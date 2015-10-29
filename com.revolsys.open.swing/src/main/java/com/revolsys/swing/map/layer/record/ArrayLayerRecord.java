package com.revolsys.swing.map.layer.record;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.equals.Equals;
import com.revolsys.equals.EqualsInstance;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.enableable.Enabled;

public class ArrayLayerRecord extends ArrayRecord implements LayerRecord {
  private static final long serialVersionUID = 1L;

  private Reference<Identifier> identifier = new WeakReference<>(null);

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

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public final void cancelChanges() {
    try (
      Enabled disabled = getLayer().eventsDisabled()) {
      synchronized (getSync()) {
        final RecordState state = getState();
        if (this.originalValues != null) {
          try {
            setState(RecordState.INITIALIZING);
            super.setValues(this.originalValues);
            this.originalValues = null;
          } finally {
            setState(state);
          }
        }
        if (state != RecordState.NEW) {
          setState(RecordState.PERSISTED);
        }
      }
    }
    firePropertyChange(EVENT_RECORD_CHANGED, false, true);
  }

  @Override
  public void clearChanges() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      this.originalValues = null;
    }
  }

  @Override
  public Identifier getIdentifier() {
    synchronized (getSync()) {
      Identifier identifier = this.identifier.get();
      if (identifier == null) {
        identifier = super.getIdentifier();
        this.identifier = new WeakReference<>(identifier);
      }
      return identifier;
    }
  }

  @Override
  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
    if (this.originalValues != null) {
      if (this.originalValues.containsKey(name)) {
        return (T)this.originalValues.get(name);
      }
    }
    return (T)getValue(name);
  }

  protected Object getSync() {
    return getLayer().getSync();
  }

  @Override
  public boolean isModified(final String name) {
    if (this.originalValues == null) {
      return false;
    } else {
      return this.originalValues.containsKey(name);
    }
  }

  @Override
  public LayerRecord revertChanges() {
    if (this.originalValues != null || getState() == RecordState.DELETED) {
      cancelChanges();
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
    if (!EqualsInstance.INSTANCE.equals(oldValue, newValue)) {
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
            if (Equals.equal(originalValue, newValue)) {
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
              if (!RecordState.INITIALIZING.equals(state)) {
                setState(RecordState.MODIFIED);
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
