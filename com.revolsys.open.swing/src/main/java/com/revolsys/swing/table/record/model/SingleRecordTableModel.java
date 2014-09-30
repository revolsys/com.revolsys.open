package com.revolsys.swing.table.record.model;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.JTableHeader;

import com.revolsys.data.record.Record;
import com.revolsys.swing.table.BaseJxTable;

public class SingleRecordTableModel extends AbstractSingleRecordTableModel {
  public static JComponent create(final Record object, final boolean editable) {
    final SingleRecordTableModel model = new SingleRecordTableModel(object,
      editable);
    final BaseJxTable table = createTable(model);
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  private static final long serialVersionUID = 1L;

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
      final String fieldName = getFieldName(rowIndex);
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
  protected Object setObjectValue(final int rowIndex, final Object value) {
    final Object oldValue = this.record.getValue(rowIndex);
    this.record.setValue(rowIndex, value);
    return oldValue;
  }

}
