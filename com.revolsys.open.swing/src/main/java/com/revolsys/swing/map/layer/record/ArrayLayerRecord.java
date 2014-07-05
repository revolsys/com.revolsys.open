package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class ArrayLayerRecord extends ArrayRecord implements LayerRecord {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private Map<String, Object> originalValues;

  private Reference<Identifier> identifier = new WeakReference<>(null);

  public ArrayLayerRecord(final AbstractRecordLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
  }

  public ArrayLayerRecord(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    super(layer.getMetaData());
    setState(RecordState.Initalizing);
    setValues(values);
    setState(RecordState.Persisted);
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public synchronized void cancelChanges() {
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

  @Override
  public void clearChanges() {
    final RecordState state = getState();
    if (state == RecordState.Persisted) {
      this.originalValues = null;
    }
  }

  @Override
  public void firePropertyChange(final String attributeName,
    final Object oldValue, final Object newValue) {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this,
        attributeName, oldValue, newValue);
      layer.propertyChange(event);
    }
  }

  @Override
  public synchronized Identifier getIdentifier() {
    Identifier identifier = this.identifier.get();
    if (identifier == null) {
      identifier = super.getIdentifier();
      this.identifier = new WeakReference<>(identifier);
    }
    return identifier;
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
      final String attributeName = getMetaData().getAttributeName(index);
      return isModified(attributeName);
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
        } else if (EqualsRegistry.equal(id, otherId)) {
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
      final RecordDefinition metaData = getMetaData();
      final String name = metaData.getAttributeName(index);
      return isValid(name);
    }
  }

  @Override
  public boolean isValid(final String name) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final Attribute attribute = getMetaData().getAttribute(name);
      if (attribute != null && attribute.isRequired()) {
        final Object value = getValue(name);
        if (value == null || value instanceof String
          && !StringUtils.hasText((String)value)) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public void postSaveChanges() {
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
    for (final String fieldName : getMetaData().getAttributeNames()) {
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
  public void setValue(final int index, final Object value) {
    final RecordDefinition metaData = getMetaData();
    final String attributeName = metaData.getAttributeName(index);

    final Object oldValue = getValue(index);
    if (!EqualsInstance.INSTANCE.equals(oldValue, value)) {
      final AbstractRecordLayer layer = getLayer();
      final RecordState state = getState();
      if (RecordState.Initalizing.equals(state)) {
        // Allow modification on initialization
      } else if (RecordState.New.equals(state)) {
        if (!layer.isCanAddRecords()) {
          throw new IllegalStateException(
            "Adding new objects is not supported for layer " + layer);
        }
      } else if (RecordState.Deleted.equals(state)) {
        throw new IllegalStateException(
          "Cannot edit a deleted object for layer " + layer);
      } else {
        if (layer.isCanEditRecords()) {
          final Object originalValue = getOriginalValue(attributeName);
          if (EqualsRegistry.equal(value, originalValue)) {
            if (this.originalValues != null) {
              this.originalValues.remove(attributeName);
              if (this.originalValues.isEmpty()) {
                this.originalValues = null;
                setState(RecordState.Persisted);
              }
            }
          } else {
            if (this.originalValues == null) {
              this.originalValues = new HashMap<String, Object>();
            }
            this.originalValues.put(attributeName, originalValue);
          }
        } else {
          throw new IllegalStateException(
            "Editing objects is not supported for layer " + layer);
        }
      }
      super.setValue(index, value);
      if (!RecordState.Initalizing.equals(state)) {
        firePropertyChange(attributeName, oldValue, value);
        layer.updateRecordState(this);
      }
    }
  }
}
