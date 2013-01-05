package com.revolsys.swing.table.dataobject;

import javax.swing.JComponent;

import com.revolsys.gis.data.model.DataObject;

@SuppressWarnings("serial")
public class DataObjectTableModel extends AbstractDataObjectTableModel {

  public static JComponent create(final DataObject object,
    final boolean editable) {
    final DataObjectTableModel model = new DataObjectTableModel(object,
      editable);
    return create(model);
  }

  private DataObject object;

  public DataObjectTableModel(final DataObject object, final boolean editable) {
    super(object.getMetaData(), editable);
    this.object = object;
  }

  public DataObject getObject() {
    return object;
  }

  @Override
  protected Object getValue(final int rowIndex) {
    return object.getValue(rowIndex);
  }

  public void setObject(final DataObject object) {
    if (object != this.object) {
      setMetaData(object.getMetaData());
      this.object = object;
      fireTableDataChanged();
    }

  }

  @Override
  protected Object setValue(final Object value, final int rowIndex) {
    final Object oldValue = object.getValue(rowIndex);
    object.setValue(rowIndex, value);
    return oldValue;
  }

}
