package com.revolsys.swing.table.dataobject.model;

import javax.swing.JTable;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.comparator.StringNumericComparator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.editor.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.renderer.SingleDataObjectTableCellRenderer;

public abstract class AbstractSingleDataObjectTableModel extends
  AbstractDataObjectTableModel {
  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  public static BaseJxTable createTable(final AbstractDataObjectTableModel model) {
    final BaseJxTable table = new BaseJxTable(model);
    table.setModel(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);

    final SingleDataObjectTableCellRenderer cellRenderer = new SingleDataObjectTableCellRenderer();
    final DataObjectTableCellEditor cellEditor = new DataObjectTableCellEditor(
      table);

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

  public AbstractSingleDataObjectTableModel(final DataObjectMetaData metaData,
    final boolean editable) {
    super(metaData);
    setEditable(editable);
  }

  @Override
  public String getAttributeName(final int row, final int column) {
    return getAttributeName(row);
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  public abstract Object getObjectValue(final int attributeIndex);

  @Override
  public int getRowCount() {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getAttributeCount();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return rowIndex;
      case 1:
        final String attributeName = getAttributeName(rowIndex);
        final DataObjectMetaData metaData = getMetaData();
        final String title = metaData.getAttributeTitle(attributeName);
        return title;
      case 2:
        return getObjectValue(rowIndex);
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (isEditable()) {
        final DataObjectMetaData metaData = getMetaData();
        if (rowIndex == metaData.getIdAttributeIndex()) {
          return false;
        } else {
          final String attributeName = getAttributeName(rowIndex);
          return !isReadOnly(attributeName);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex,
    final int columnIndex) {
    return selected;
  }

  protected Object setDisplayValue(final int attributeIndex,
    final Object displayValue) {
    final Object objectValue = toObjectValue(attributeIndex, displayValue);
    return setObjectValue(attributeIndex, objectValue);
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    super.setMetaData(metaData);
  }

  protected abstract Object setObjectValue(final int attributeIndex,
    final Object value);

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {

      final Object oldValue = setDisplayValue(rowIndex, value);
      final String propertyName = getAttributeName(rowIndex);
      firePropertyChange(propertyName, oldValue, value);
    }
  }
}
