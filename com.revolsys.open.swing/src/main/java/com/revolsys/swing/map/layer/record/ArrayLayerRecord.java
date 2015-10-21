package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.equals.Equals;
import com.revolsys.equals.EqualsInstance;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.util.Property;

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
    setState(RecordState.Initalizing);
    setValues(values);
    setState(RecordState.Persisted);
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public void cancelChanges() {
    synchronized (getSync()) {
      RecordState newState = getState();
      if (newState != RecordState.New) {
        newState = RecordState.Persisted;
      }
      setState(RecordState.Initalizing);

      if (this.originalValues != null) {
        super.setValues(this.originalValues);
      }
      this.originalValues = null;
      setState(newState);
    }
  }

  @Override
  public void clearChanges() {
    final RecordState state = getState();
    if (state == RecordState.Persisted) {
      this.originalValues = null;
    }
  }

  @Override
  public void firePropertyChange(final String fieldName, final Object oldValue,
    final Object newValue) {
    final AbstractLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this, fieldName, oldValue,
        newValue);
      layer.propertyChange(event);
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
  public boolean isDeletable() {
    if (this.layer.isCanDeleteRecords()) {
      return !isDeleted();
    }
    return false;
  }

  @Override
  public boolean isDeleted() {
    return getState() == RecordState.Deleted;
  }

  @Override
  public boolean isGeometryEditable() {
    return true;
  }

  @Override
  public boolean isModified() {
    return super.isModified();
  }

  @Override
  public boolean isModified(final int index) {
    if (this.originalValues == null) {
      return false;
    } else {
      final String fieldName = getRecordDefinition().getFieldName(index);
      return isModified(fieldName);
    }
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
  public boolean isSame(final Record record) {
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
  public boolean isValid(final int index) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String name = recordDefinition.getFieldName(index);
      return isValid(name);
    }
  }

  @Override
  public boolean isValid(final String name) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final FieldDefinition attribute = getRecordDefinition().getField(name);
      if (attribute != null && attribute.isRequired()) {
        final Object value = getValue(name);
        if (value == null || value instanceof String && !Property.hasValue((String)value)) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public void postSaveDeleted() {
  }

  @Override
  public void postSaveModified() {
    if (getState() == RecordState.Persisted) {
      clearChanges();
    }
  }

  @Override
  public void postSaveNew() {
  }

  @Override
  public LayerRecord revertChanges() {
    if (this.originalValues != null || getState() == RecordState.Deleted) {
      cancelChanges();
      final AbstractRecordLayer layer = getLayer();
      layer.revertChanges(this);
      firePropertyChange("state", RecordState.Modified, RecordState.Persisted);
    }
    return this;
  }

  @Override
  public void revertEmptyFields() {
    for (final String fieldName : getRecordDefinition().getFieldNames()) {
      final Object value = getValue(fieldName);
      if (Property.isEmpty(value)) {
        if (!this.layer.isFieldUserReadOnly(fieldName)) {
          final Object originalValue = getOriginalValue(fieldName);
          if (!Property.isEmpty(originalValue)) {
            setValue(fieldName, originalValue);
          }
        }
      }
    }
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    boolean updated = false;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String fieldName = recordDefinition.getFieldName(index);

    final Object oldValue = getValue(index);
    if (!EqualsInstance.INSTANCE.equals(oldValue, value)) {
      final AbstractRecordLayer layer = getLayer();
      final RecordState state = getState();
      if (RecordState.Initalizing.equals(state)) {
        // Allow modification on initialization
      } else if (RecordState.New.equals(state)) {
        if (!layer.isCanAddRecords()) {
          throw new IllegalStateException("Adding new records is not supported for layer " + layer);
        }
      } else if (RecordState.Deleted.equals(state)) {
        throw new IllegalStateException("Cannot edit a deleted record for layer " + layer);
      } else {
        if (layer.isCanEditRecords()) {
          final Object originalValue = getOriginalValue(fieldName);
          if (Equals.equal(value, originalValue)) {
            if (this.originalValues != null) {
              this.originalValues.remove(fieldName);
              if (this.originalValues.isEmpty()) {
                this.originalValues = null;
                setState(RecordState.Persisted);
              }
            }
          } else {
            if (this.originalValues == null) {
              this.originalValues = new HashMap<>();
            }
            this.originalValues.put(fieldName, originalValue);
          }
        } else {
          throw new IllegalStateException("Editing records is not supported for layer " + layer);
        }
      }
      updated |= super.setValue(index, value);
      if (!RecordState.Initalizing.equals(state)) {
        firePropertyChange(fieldName, oldValue, value);
        layer.updateRecordState(this);
      }
    }
    return updated;
  }

  @Override
  public void validate() {
  }
}
