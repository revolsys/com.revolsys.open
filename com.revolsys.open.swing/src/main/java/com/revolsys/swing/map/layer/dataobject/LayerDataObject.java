package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class LayerDataObject extends ArrayDataObject {
  private static final long serialVersionUID = 1L;

  private final DataObjectLayer layer;

  private Map<String, Object> originalValues;

  public LayerDataObject(final DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
  }

  protected void clearOriginalValues() {
    if (originalValues != null) {
      super.setValues(originalValues);
    }
    originalValues = null;
  }

  public void firePropertyChange(final String attributeName,
    final Object oldValue, final Object newValue) {
    final DataObjectLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this,
        attributeName, oldValue, newValue);
      layer.propertyChange(event);
    }
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
    if (originalValues != null) {
      if (originalValues.containsKey(name)) {
        return (T)originalValues.get(name);
      }
    }
    return (T)getValue(name);
  }

  public boolean isDeletable() {
    if (layer.isCanDeleteRecords()) {
      return isDeleted();
    }
    return false;
  }

  public boolean isDeleted() {
    return getState() == DataObjectState.Deleted;
  }

  public boolean isGeometryEditable() {
    return true;
  }

  @Override
  public boolean isModified() {
    return originalValues != null;
  }

  public boolean isModified(final int index) {
    if (originalValues == null) {
      return false;
    } else {
      final String attributeName = getMetaData().getAttributeName(index);
      return isModified(attributeName);
    }
  }

  public boolean isModified(final String name) {
    if (originalValues == null) {
      return false;
    } else {
      return originalValues.containsKey(name);
    }
  }

  @Override
  public boolean isValid(final int index) {
    final DataObjectMetaData metaData = getMetaData();
    final String name = metaData.getAttributeName(index);
    return isValid(name);

  }

  public boolean isValid(final String name) {
    final Attribute attribute = getMetaData().getAttribute(name);
    if (attribute.isRequired()) {
      final Object value = getValue(name);
      if (value == null
        || ((value instanceof String) && !StringUtils.hasText((String)value))) {
        return false;
      }
    }
    return true;
  }

  public void revertChanges() {
    if (originalValues != null || getState() == DataObjectState.Deleted) {
      setState(DataObjectState.Persisted);
      clearOriginalValues();
      setState(DataObjectState.Persisted);
      final DataObjectLayer layer = getLayer();
      layer.revertChanges(this);
      firePropertyChange("state", DataObjectState.Modified,
        DataObjectState.Persisted);
    }
  }

  @Override
  public void setValue(final int index, final Object value) {
    final DataObjectMetaData metaData = getMetaData();
    final String attributeName = metaData.getAttributeName(index);

    final Object oldValue = getValue(index);
    if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
      final DataObjectLayer layer = getLayer();
      final DataObjectState state = getState();
      if (DataObjectState.Initalizing.equals(state)) {
        // Allow modification on initialization
      } else if (DataObjectState.New.equals(state)) {
        if (!layer.isCanAddRecords()) {
          throw new IllegalStateException(
            "Adding new objects is not supported for layer " + layer);
        }
      } else if (DataObjectState.Deleted.equals(state)) {
        throw new IllegalStateException(
          "Cannot edit a deleted object for layer " + layer);
      } else {
        if (layer.isCanEditRecords()) {
          final Object originalValue = getOriginalValue(attributeName);
          if (EqualsRegistry.equal(value, originalValue)) {
            if (originalValues != null) {
              originalValues.remove(attributeName);
              if (originalValues.isEmpty()) {
                originalValues = null;
                setState(DataObjectState.Persisted);
              }
            }
          } else {
            if (originalValues == null) {
              originalValues = new HashMap<String, Object>();
            }
            originalValues.put(attributeName, originalValue);
          }
        } else {
          throw new IllegalStateException(
            "Editing objects is not supported for layer " + layer);
        }
      }
      super.setValue(index, value);
      if (!DataObjectState.Initalizing.equals(state)) {
        firePropertyChange(attributeName, oldValue, value);
      }
    }
  }
}
