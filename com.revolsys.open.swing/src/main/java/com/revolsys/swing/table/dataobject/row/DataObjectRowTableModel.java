package com.revolsys.swing.table.dataobject.row;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.table.SortableTableModel;

public abstract class DataObjectRowTableModel extends AbstractTableModel
  implements SortableTableModel {
  private static final long serialVersionUID = 1L;

  private List<String> columnIndexNames = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private DataObjectMetaData metaData;

  private boolean editable;

  public DataObjectRowTableModel(final DataObjectMetaData metaData) {
    this(metaData, metaData.getAttributeNames());
  }

  public DataObjectRowTableModel(final DataObjectMetaData metaData,
    final List<String> columnIndexNames) {
    this.metaData = metaData;
    this.columnIndexNames = columnIndexNames;
  }

  public void clearSortedColumns() {
    synchronized (sortedColumns) {
      sortedColumns.clear();
      fireTableDataChanged();
    }
  }

  @PreDestroy
  public void dispose() {
    metaData = null;
    sortedColumns = null;
  }

  public String getAttributeName(final int columnIndex) {
    return columnIndexNames.get(columnIndex);
  }

  public Attribute getColumnAttribute(final int columnIndex) {
    final String name = getAttributeName(columnIndex);
    return metaData.getAttribute(name);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    final String name = getAttributeName(columnIndex);
    final DataType type = metaData.getAttributeType(name);
    if (type == null) {
      return Object.class;
    } else {
      return type.getJavaClass();
    }
  }

  @Override
  public int getColumnCount() {
    return columnIndexNames.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    // TODO columnIndex titles
    return columnIndexNames.get(columnIndex);
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public abstract DataObject getObject(final int row);

  public List<DataObject> getObjects(final int[] rows) {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final int row : rows) {
      final DataObject object = getObject(row);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  public Map<Integer, SortOrder> getSortedColumns() {
    return sortedColumns;
  }

  @Override
  public SortOrder getSortOrder(final int columnIndex) {
    synchronized (sortedColumns) {
      return sortedColumns.get(columnIndex);
    }
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      final String name = getAttributeName(columnIndex);
      return object.getValue(name);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return isEditable();
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  @Override
  public SortOrder setSortOrder(final int columnIndex) {
    synchronized (sortedColumns) {
      SortOrder sortOrder = sortedColumns.get(columnIndex);
      sortedColumns.clear();
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
      sortedColumns.put(columnIndex, sortOrder);

      fireTableDataChanged();
      return sortOrder;
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    throw new UnsupportedOperationException(
      "Editing is not currently supoorted");
  }
}
