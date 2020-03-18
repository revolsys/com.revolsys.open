package com.revolsys.swing.table.lambda.column;

import com.revolsys.record.Record;

public class RecordTableModelColumn extends AbstractTableModelColumn {

  protected final Record record;

  public RecordTableModelColumn(final String columnName, final Record record,
    final boolean editable) {
    super(columnName, Object.class, editable);
    this.record = record;
  }

  @Override
  public Object getValueAt(final int rowIndex) {
    return this.record.getCodeValue(rowIndex);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex) {
    this.record.setValue(rowIndex, value);
  }
}
