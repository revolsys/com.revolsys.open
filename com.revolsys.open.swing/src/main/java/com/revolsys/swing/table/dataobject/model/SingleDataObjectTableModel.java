package com.revolsys.swing.table.dataobject.model;

import javax.swing.JComponent;
import javax.swing.table.JTableHeader;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.table.BaseJxTable;

public class SingleDataObjectTableModel extends
  AbstractSingleDataObjectTableModel {
  private static final long serialVersionUID = 1L;

  public static JComponent create(final DataObject object,
    final boolean editable) {
    final SingleDataObjectTableModel model = new SingleDataObjectTableModel(
      object, editable);
    final BaseJxTable table = createTable(model);
    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);
    return table;
  }

  private DataObject object;

  public SingleDataObjectTableModel(final DataObject object,
    final boolean editable) {
    super(object.getMetaData(), editable);
    this.object = object;
  }

  public DataObject getObject() {
    return this.object;
  }

  @Override
  public Object getObjectValue(final int rowIndex) {
    if (this.object == null) {
      return "\u2026";
    } else {
      return this.object.getValue(rowIndex);
    }
  }

  public void setObject(final DataObject object) {
    if (object != this.object) {
      setMetaData(object.getMetaData());
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
