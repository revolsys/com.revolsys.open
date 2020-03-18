package com.revolsys.swing.table.record.model;

import java.util.Map;

import javax.swing.table.JTableHeader;

import com.revolsys.record.Record;
import com.revolsys.swing.table.BaseJTable;

public class SingleRecordTableModel extends AbstractSingleRecordTableModel {
  private static final long serialVersionUID = 1L;

  public static BaseJTable newTable(final Record object, final boolean editable) {
    final SingleRecordTableModel model = new SingleRecordTableModel(object, editable);
    final BaseJTable table = newTable(model);
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  private Record record;

  public SingleRecordTableModel(final Record object, final boolean editable) {
    super(object.getRecordDefinition(), editable);
    this.record = object;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Map<String, Object>> V getMap(final int columnIndex) {
    if (columnIndex == 2) {
      return (V)this.record;
    } else {
      return null;
    }
  }

  public Record getObject() {
    return this.record;
  }

  @Override
  public Object getObjectValue(final int rowIndex, final int columnIndex) {
    if (this.record == null) {
      return "\u2026";
    } else {
      final String fieldName = getColumnFieldName(rowIndex);
      return this.record.getValue(fieldName);
    }
  }

  public void setObject(final Record object) {
    if (object != this.record) {
      setRecordDefinition(object.getRecordDefinition());
      this.record = object;
      fireTableDataChanged();
    }

  }

  @Override
  protected Object setObjectValue(final String fieldName, final Object value) {
    final Object oldValue = this.record.getValue(fieldName);
    this.record.setValue(fieldName, value);
    return oldValue;
  }

}
