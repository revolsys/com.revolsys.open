package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.AbstractRecord;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public abstract class AbstractLayerRecord extends AbstractRecord implements
LayerRecord {

  private final AbstractDataObjectLayer layer;

  public AbstractLayerRecord(final AbstractDataObjectLayer layer) {
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public synchronized void cancelChanges() {
  }

  @Override
  public void clearChanges() {
  }

  @Override
  public void firePropertyChange(final String attributeName,
    final Object oldValue, final Object newValue) {
    final AbstractDataObjectLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this,
        attributeName, oldValue, newValue);
      layer.propertyChange(event);
    }
  }

  @Override
  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    final AbstractDataObjectLayer layer = getLayer();
    return layer.getMetaData();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
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
    return getState() == DataObjectState.Deleted;
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
    final String attributeName = getMetaData().getAttributeName(index);
    return isModified(attributeName);
  }

  @Override
  public boolean isModified(final String name) {
    return false;
  }

  @Override
  public boolean isSame(final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (this == record) {
      return true;
    } else {
      final AbstractDataObjectLayer layer = getLayer();
      if (layer.isLayerRecord(record)) {
        final RecordIdentifier id = getIdentifier();
        final RecordIdentifier otherId = record.getIdentifier();
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
    if (getState() == DataObjectState.Initalizing) {
      return true;
    } else {
      final DataObjectMetaData metaData = getMetaData();
      final String name = metaData.getAttributeName(index);
      return isValid(name);
    }
  }

  @Override
  public boolean isValid(final String name) {
    if (getState() == DataObjectState.Initalizing) {
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
  public LayerRecord revertChanges() {
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

}
