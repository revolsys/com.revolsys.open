package com.revolsys.swing.table.record.model;

import java.util.Map;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.table.BaseJTable;

public class RecordMapTableModel extends AbstractSingleRecordTableModel {

  private static final long serialVersionUID = 1L;

  public static BaseJTable newTable(final RecordDefinition recordDefinition,
    final Map<String, Object> values, final boolean editable) {
    final RecordMapTableModel model = new RecordMapTableModel(recordDefinition, values, editable);
    return newTable(model);
  }

  private Map<String, Object> values;

  public RecordMapTableModel(final RecordDefinition recordDefinition,
    final Map<String, Object> values, final boolean editable) {
    super(recordDefinition, editable);
    this.values = values;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Map<String, Object>> V getMap(final int columnIndex) {
    if (columnIndex == 2) {
      return (V)this.values;
    } else {
      return null;
    }
  }

  @Override
  public Object getObjectValue(final int attributeIndex, final int columnIndex) {
    final String name = getColumnFieldName(attributeIndex);
    return this.values.get(name);
  }

  public Map<String, ? extends Object> getValues() {
    return this.values;
  }

  @Override
  protected Object setObjectValue(final String fieldName, final Object value) {
    final Object oldValue = this.values.get(fieldName);
    this.values.put(fieldName, value);
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
