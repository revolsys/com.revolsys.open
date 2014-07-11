package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;

import org.springframework.util.StringUtils;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.AbstractRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public abstract class AbstractLayerRecord extends AbstractRecord implements
  LayerRecord {

  private final AbstractRecordLayer layer;

  public AbstractLayerRecord(final AbstractRecordLayer layer) {
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public void cancelChanges() {
  }

  @Override
  public void clearChanges() {
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
  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
    return (T)getValue(name);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.getRecordDefinition();
    }
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
    final String attributeName = getRecordDefinition().getAttributeName(index);
    return isModified(attributeName);
  }

  @Override
  public boolean isModified(final String name) {
    return false;
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
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String name = recordDefinition.getAttributeName(index);
      return isValid(name);
    }
  }

  @Override
  public boolean isValid(final String name) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final Attribute attribute = getRecordDefinition().getAttribute(name);
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
    return this;
  }

  @Override
  public void revertEmptyFields() {
    for (final String fieldName : getRecordDefinition().getAttributeNames()) {
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

}
