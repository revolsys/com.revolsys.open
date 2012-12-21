package com.revolsys.swing.table.dataobject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.gis.data.model.DataObjectMetaData;

@SuppressWarnings("serial")
public abstract class AbstractDataObjectTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {
    "Attribute", "Value"
  };

  public static JTable create(final AbstractDataObjectTableModel model) {
    final JTable table = new JTable(model);
    table.setModel(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    table.setAutoCreateColumnsFromModel(false);

    final DataObjectTableCellRenderer cellRenderer = new DataObjectTableCellRenderer();
    final DataObjectTableCellEditor cellEditor = new DataObjectTableCellEditor();

    final JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);

    final TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      column.setCellRenderer(cellRenderer);
      if (i == 1) {
        column.setCellEditor(cellEditor);
      }
    }
    return table;
  }

  protected final boolean editable;

  private DataObjectMetaData metaData;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public AbstractDataObjectTableModel(DataObjectMetaData metaData,
    boolean editable) {
    this.metaData = metaData;
    this.editable = editable;
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  public String getAttributeName(final int rowIndex) {
    return metaData.getAttributeName(rowIndex);
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  @Override
  public int getRowCount() {
    return metaData.getAttributeCount();

  }

  protected abstract Object getValue(final int rowIndex);

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return getAttributeName(rowIndex);
      case 1:
        return getValue(rowIndex);
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 1) {
      if (rowIndex == metaData.getIdAttributeIndex()) {
        return false;
      } else {
        return editable;
      }
    } else {
      return false;
    }
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
  }

  public void setMetaData(DataObjectMetaData metaData) {
    if (metaData != this.metaData) {
      fireTableStructureChanged();
      this.metaData = metaData;
    }
  }

  protected abstract Object setValue(final Object value, final int rowIndex);

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {
      final Object oldValue = setValue(value, rowIndex);
      final String propertyName = getAttributeName(rowIndex);
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, value);
    }
  }
}
