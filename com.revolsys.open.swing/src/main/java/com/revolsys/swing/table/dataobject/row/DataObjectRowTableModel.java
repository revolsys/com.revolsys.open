package com.revolsys.swing.table.dataobject.row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.SortableTableModel;

public abstract class DataObjectRowTableModel extends AbstractTableModel
  implements SortableTableModel {
  private static final long serialVersionUID = 1L;

  private List<String> attributeNames = new ArrayList<String>();

  private final List<String> attributeTitles = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private DataObjectMetaData metaData;

  private boolean editable;

  private DataObjectRowTable table;

  public DataObjectRowTableModel(final DataObjectMetaData metaData,
    final Collection<String> attributeNames) {
    this.metaData = metaData;
    setAttributeNames(attributeNames);
    setAttributeTitles(Collections.<String> emptyList());
  }

  public void clearSortedColumns() {
    synchronized (this.sortedColumns) {
      this.sortedColumns.clear();
      fireTableDataChanged();
    }
  }

  @PreDestroy
  public void dispose() {
    this.metaData = null;
    this.sortedColumns = null;
  }

  public String getAttributeName(final int columnIndex) {
    final String attributeName = this.attributeNames.get(columnIndex);
    final int index = attributeName.indexOf('.');
    if (index == -1) {
      return attributeName;
    } else {
      return attributeName.substring(0, index);
    }
  }

  public List<String> getAttributeTitles() {
    return this.attributeTitles;
  }

  public Attribute getColumnAttribute(final int columnIndex) {
    final String name = getAttributeName(columnIndex);
    return this.metaData.getAttribute(name);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    final String name = getAttributeName(columnIndex);
    final DataType type = this.metaData.getAttributeType(name);
    if (type == null) {
      return Object.class;
    } else {
      return type.getJavaClass();
    }
  }

  @Override
  public int getColumnCount() {
    final int numColumns = this.attributeNames.size();
    return numColumns;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return this.attributeTitles.get(columnIndex);
  }

  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public abstract LayerDataObject getObject(final int row);

  public List<LayerDataObject> getObjects(final int[] rows) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (final int row : rows) {
      final LayerDataObject object = getObject(row);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  public Map<Integer, SortOrder> getSortedColumns() {
    return this.sortedColumns;
  }

  @Override
  public SortOrder getSortOrder(final int columnIndex) {
    synchronized (this.sortedColumns) {
      return this.sortedColumns.get(columnIndex);
    }
  }

  public DataObjectRowTable getTable() {
    return this.table;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final LayerDataObject object = getObject(rowIndex);
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
    return this.editable;
  }

  public void setAttributeNames(final Collection<String> attributeNames) {
    if (attributeNames == null || attributeNames.isEmpty()) {
      this.attributeNames = new ArrayList<String>(
        this.metaData.getAttributeNames());
    } else {
      this.attributeNames = new ArrayList<String>(attributeNames);
    }
  }

  public void setAttributeTitles(final List<String> attributeTitles) {
    this.attributeTitles.clear();
    for (int i = 0; i < this.attributeNames.size(); i++) {
      String title;
      if (i < attributeTitles.size()) {
        title = attributeTitles.get(i);
      } else {
        final String attributeName = getAttributeName(i);
        final DataObjectMetaData metaData = getMetaData();
        final Attribute attribute = metaData.getAttribute(attributeName);
        title = attribute.getTitle();
      }
      this.attributeTitles.add(title);
    }
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  @Override
  public SortOrder setSortOrder(final int columnIndex) {
    synchronized (this.sortedColumns) {
      SortOrder sortOrder = this.sortedColumns.get(columnIndex);
      this.sortedColumns.clear();
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
      this.sortedColumns.put(columnIndex, sortOrder);

      fireTableDataChanged();
      return sortOrder;
    }
  }

  public void setTable(final DataObjectRowTable table) {
    this.table = table;
    addTableModelListener(table);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    throw new UnsupportedOperationException(
      "Editing is not currently supoorted");
  }
}
