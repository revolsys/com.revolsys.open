package com.revolsys.swing.table.dataobject;

import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.gis.data.model.DataObjectMetaData;

@SuppressWarnings("serial")
public class DataObjectMapTableModel extends AbstractDataObjectTableModel {

  public static JComponent create(final DataObjectMetaData metaData,
    final Map<String, Object> values, final boolean editable) {
    final DataObjectMapTableModel model = new DataObjectMapTableModel(metaData,
      values, editable);
    return create(model);
  }

  private Map<String, Object> values;

  public DataObjectMapTableModel(final DataObjectMetaData metaData,
    final Map<String, Object> values, final boolean editable) {
    super(metaData, editable);
    this.values = values;
  }

  @Override
  protected Object getValue(final int rowIndex) {
    final String name = getAttributeName(rowIndex);
    return values.get(name);
  }

  public Map<String, ? extends Object> getValues() {
    return values;
  }

  @Override
  protected Object setValue(final Object value, final int rowIndex) {
    final String name = getAttributeName(rowIndex);
    final Object oldValue = values.get(name);
    values.put(name, value);
    return oldValue;
  }

  public void setValue(final String fieldName, final Object fieldValue) {
    values.put(fieldName, fieldValue);
    fireTableDataChanged();
  }

  public void setValues(final Map<String, Object> values) {
    if (values != this.values) {
      this.values = values;
    }
    fireTableDataChanged();
  }

}
