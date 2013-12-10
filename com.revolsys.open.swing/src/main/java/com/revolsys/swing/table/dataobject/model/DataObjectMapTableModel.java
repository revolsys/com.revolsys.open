package com.revolsys.swing.table.dataobject.model;

import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectMapTableModel extends AbstractSingleDataObjectTableModel {

  private static final long serialVersionUID = 1L;

  public static JComponent create(final DataObjectMetaData metaData,
    final Map<String, Object> values, final boolean editable) {
    final DataObjectMapTableModel model = new DataObjectMapTableModel(metaData,
      values, editable);
    return createTable(model);
  }

  private Map<String, Object> values;

  public DataObjectMapTableModel(final DataObjectMetaData metaData,
    final Map<String, Object> values, final boolean editable) {
    super(metaData, editable);
    this.values = values;
  }

  @Override
  public Object getObjectValue(final int attributeIndex) {
    final String name = getFieldName(attributeIndex);
    return this.values.get(name);
  }

  public Map<String, ? extends Object> getValues() {
    return this.values;
  }

  @Override
  protected Object setObjectValue(final int rowIndex, final Object value) {
    final String name = getFieldName(rowIndex);
    final Object oldValue = this.values.get(name);
    this.values.put(name, value);
    return oldValue;
  }

  public void setValue(final String fieldName, final Object fieldValue) {
    this.values.put(fieldName, fieldValue);
    fireTableDataChanged();
  }

  public void setValues(final Map<String, Object> values) {
    if (values != this.values) {
      this.values = values;
    }
    fireTableDataChanged();
  }

}
