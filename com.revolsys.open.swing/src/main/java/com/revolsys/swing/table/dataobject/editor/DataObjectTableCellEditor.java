package com.revolsys.swing.table.dataobject.editor;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.table.dataobject.model.AbstractDataObjectTableModel;

public class DataObjectTableCellEditor extends AbstractCellEditor implements
  TableCellEditor {

  private static final long serialVersionUID = 1L;

  private JComponent editorComponent;

  private String attributeName;

  private final AbstractDataObjectTableModel model;

  public DataObjectTableCellEditor(final AbstractDataObjectTableModel model) {
    this.model = model;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  @Override
  public Object getCellEditorValue() {
    return SwingUtil.getValue(this.editorComponent);
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    this.attributeName = model.getAttributeName(rowIndex, columnIndex);
    final DataObjectMetaData metaData = model.getMetaData();
    this.editorComponent = (JComponent)SwingUtil.createField(metaData,
      this.attributeName, true);
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setHorizontalAlignment(SwingConstants.LEFT);
    }
    SwingUtil.setFieldValue(this.editorComponent, value);
    return this.editorComponent;
  }

  @Override
  public boolean isCellEditable(final EventObject event) {
    return model.isCellEditable(event);
  }
}
