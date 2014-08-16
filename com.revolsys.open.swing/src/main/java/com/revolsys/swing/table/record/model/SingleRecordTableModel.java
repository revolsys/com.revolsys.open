package com.revolsys.swing.table.record.model;

import javax.swing.JComponent;
import javax.swing.table.JTableHeader;

import com.revolsys.data.record.Record;
import com.revolsys.swing.table.BaseJxTable;

public class SingleRecordTableModel extends
  AbstractSingleRecordTableModel {
  private static final long serialVersionUID = 1L;

  public static JComponent create(final Record object,
    final boolean editable) {
    final SingleRecordTableModel model = new SingleRecordTableModel(
      object, editable);
    final BaseJxTable table = createTable(model);
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  private Record object;

  public SingleRecordTableModel(final Record object,
    final boolean editable) {
    super(object.getRecordDefinition(), editable);
    this.object = object;
  }

  public Record getObject() {
    return this.object;
  }

  @Override
  public Object getObjectValue(final int rowIndex, int columnIndex) {
    if (this.object == null) {
      return "\u2026";
    } else {
      return this.object.getValue(rowIndex);
    }
  }

  public void setObject(final Record object) {
    if (object != this.object) {
      setRecordDefinition(object.getRecordDefinition());
      this.object = object;
      fireTableDataChanged();
    }

  }

  @Override
  protected Object setObjectValue(final int rowIndex, final Object value) {
    final Object oldValue = this.object.getValue(rowIndex);
    this.object.setValue(rowIndex, value);
    return oldValue;
  }

}
