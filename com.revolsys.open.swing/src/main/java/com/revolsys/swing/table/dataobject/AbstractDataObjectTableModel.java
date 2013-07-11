package com.revolsys.swing.table.dataobject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.comparator.StringNumericComparator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.table.BaseJxTable;

@SuppressWarnings("serial")
public abstract class AbstractDataObjectTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  public static BaseJxTable create(final AbstractDataObjectTableModel model) {
    final BaseJxTable table = new BaseJxTable(model);
    table.setModel(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);

    final DataObjectTableCellRenderer cellRenderer = new DataObjectTableCellRenderer();
    final DataObjectTableCellEditor cellEditor = new DataObjectTableCellEditor();

    final DataObjectMetaData metaData = model.getMetaData();

    int maxTitleWidth = 100;
    for (final Attribute attribute : metaData.getAttributes()) {
      final String title = attribute.getTitle();
      final int titleWidth = title.length() * 7;
      if (titleWidth > maxTitleWidth) {
        maxTitleWidth = titleWidth;
      }

    }

    final int columnCount = model.getColumnCount();
    int columnWidth;
    if (columnCount > 3) {
      columnWidth = (740 - maxTitleWidth) / 2;
    } else {
      columnWidth = (740 - maxTitleWidth) / 2;
    }
    for (int i = 0; i < columnCount; i++) {
      final TableColumnExt column = table.getColumnExt(i);
      column.setCellRenderer(cellRenderer);
      if (i == 0) {
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(40);
        column.setComparator(new StringNumericComparator());
      } else if (i == 1) {
        column.setMinWidth(maxTitleWidth);
        column.setPreferredWidth(maxTitleWidth);
        column.setMaxWidth(maxTitleWidth);
      } else {
        column.setPreferredWidth(columnWidth);
        if (i == 2) {
          column.setCellEditor(cellEditor);
        }
      }
    }
    return table;
  }

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private boolean editable;

  private DataObjectMetaData metaData;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public AbstractDataObjectTableModel(final DataObjectMetaData metaData,
    final boolean editable) {
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
    return 3;
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

  public Set<String> getReadOnlyFieldNames() {
    return readOnlyFieldNames;
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
        return rowIndex;
      case 1:
        final String attributeName = getAttributeName(rowIndex);
        final String title = metaData.getAttributeTitle(attributeName);
        return title;
      case 2:
        return getValue(rowIndex);
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (editable) {
        if (rowIndex == metaData.getIdAttributeIndex()) {
          return false;
        } else {
          final String attributeName = getAttributeName(rowIndex);
          return !readOnlyFieldNames.contains(attributeName);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean isEditable() {
    return editable;
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    if (metaData != this.metaData) {
      fireTableStructureChanged();
      this.metaData = metaData;
    }
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
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
