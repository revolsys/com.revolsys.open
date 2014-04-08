package com.revolsys.swing.table.dataobject.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.table.AbstractTableModel;

import org.springframework.util.StringUtils;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;
import com.revolsys.jts.geom.Geometry;

public abstract class AbstractDataObjectTableModel extends AbstractTableModel
  implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private DataObjectMetaData metaData;

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private boolean editable;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public AbstractDataObjectTableModel() {
    this(null);
  }

  public AbstractDataObjectTableModel(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    Property.addListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void addReadOnlyFieldNames(final String... readOnlyFieldNames) {
    if (readOnlyFieldNames != null) {
      this.readOnlyFieldNames.addAll(Arrays.asList(readOnlyFieldNames));
    }
  }

  @PreDestroy
  public void dispose() {
    this.metaData = null;
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  protected void firePropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    propertyChangeSupport.fireIndexedPropertyChange(propertyName, index,
      oldValue, newValue);
  }

  protected void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public String getFieldName(final int attributeIndex) {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getAttributeName(attributeIndex);
  }

  public abstract String getFieldName(int rowIndex, int columnIndex);

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isReadOnly(final String attributeName) {
    return this.readOnlyFieldNames.contains(attributeName);
  }

  public abstract boolean isSelected(boolean selected, int rowIndex,
    int columnIndex);

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    Property.removeListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  protected void setMetaData(final DataObjectMetaData metaData) {
    if (metaData != this.metaData) {
      this.metaData = metaData;
      fireTableStructureChanged();
    }
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    if (readOnlyFieldNames == null) {
      this.readOnlyFieldNames = new HashSet<String>();
    } else {
      this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
    }
  }

  public String toDisplayValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    String text;
    final DataObjectMetaData metaData = getMetaData();
    final String idFieldName = metaData.getIdAttributeName();
    final String name = getFieldName(attributeIndex);
    if (objectValue == null) {
      if (name.equals(idFieldName)) {
        return "NEW";
      } else {
        text = "-";
      }
    } else {
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.getGeometryType();
      }
      CodeTable codeTable = null;
      if (!name.equals(idFieldName)) {
        codeTable = metaData.getCodeTableByColumn(name);
      }
      if (codeTable == null) {
        text = StringConverterRegistry.toString(objectValue);
      } else {
        final List<Object> values = codeTable.getValues(objectValue);
        if (values == null || values.isEmpty()) {
          text = "-";
        } else {
          text = CollectionUtil.toString(values);
        }
      }
      if (text.length() == 0) {
        text = "-";
      }
    }
    return text;
  }

  public Object toObjectValue(final int attributeIndex,
    final Object displayValue) {
    if (displayValue == null) {
      return null;
    }
    if (displayValue instanceof String) {
      final String string = (String)displayValue;
      if (!StringUtils.hasLength(string)) {
        return null;
      }
    }
    final DataObjectMetaData metaData = getMetaData();
    final String name = getFieldName(attributeIndex);
    final CodeTable codeTable = metaData.getCodeTableByColumn(name);
    if (codeTable == null) {
      final Class<?> fieldClass = metaData.getAttributeClass(name);
      final Object objectValue = StringConverterRegistry.toObject(fieldClass,
        displayValue);
      return objectValue;
    } else {
      final Object objectValue = codeTable.getId(displayValue);
      return objectValue;
    }
  }

}
