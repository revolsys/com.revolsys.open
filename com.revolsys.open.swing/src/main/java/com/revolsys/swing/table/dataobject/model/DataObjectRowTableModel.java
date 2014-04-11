package com.revolsys.swing.table.dataobject.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.swing.SortOrder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public abstract class DataObjectRowTableModel extends
  AbstractDataObjectTableModel implements SortableTableModel,
  CellEditorListener {

  public static final String LOADING_VALUE = "\u2026";

  private static final long serialVersionUID = 1L;

  private List<String> attributeNames = new ArrayList<String>();

  private final List<String> attributeTitles = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private DataObjectRowTable table;

  /** The columnIndex that the attribute start. Allows for extra columns in subclasses.*/
  private int attributesOffset;

  public DataObjectRowTableModel(final DataObjectMetaData metaData,
    final Collection<String> attributeNames) {
    super(metaData);
    setAttributeNames(attributeNames);
    setAttributeTitles(Collections.<String> emptyList());
    final String idAttributeName = metaData.getIdAttributeName();
    setSortOrder(idAttributeName);
  }

  public void clearSortedColumns() {
    synchronized (this.sortedColumns) {
      this.sortedColumns.clear();
      fireTableDataChanged();
    }
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.sortedColumns = null;
  }

  @Override
  public void editingCanceled(final ChangeEvent event) {
  }

  @Override
  public void editingStopped(final ChangeEvent event) {
  }

  public int getAttributesOffset() {
    return attributesOffset;
  }

  public List<String> getAttributeTitles() {
    return this.attributeTitles;
  }

  public Attribute getColumnAttribute(final int columnIndex) {
    if (columnIndex < attributesOffset) {
      return null;
    } else {
      final String name = getFieldName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      return metaData.getAttribute(name);
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex < attributesOffset) {
      return Object.class;
    } else {
      final String name = getFieldName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType type = metaData.getAttributeType(name);
      if (type == null) {
        return Object.class;
      } else {
        return type.getJavaClass();
      }
    }
  }

  @Override
  public int getColumnCount() {
    final int numColumns = attributesOffset + this.attributeNames.size();
    return numColumns;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex < attributesOffset) {
      return null;
    } else {
      return this.attributeTitles.get(columnIndex - attributesOffset);
    }
  }

  @Override
  public String getFieldName(final int columnIndex) {
    if (columnIndex < attributesOffset) {
      return null;
    } else {
      final String attributeName = this.attributeNames.get(columnIndex
        - attributesOffset);
      if (attributeName == null) {
        return null;
      } else {
        final int index = attributeName.indexOf('.');
        if (index == -1) {
          return attributeName;
        } else {
          return attributeName.substring(0, index);
        }
      }
    }
  }

  @Override
  public String getFieldName(final int rowIndex, final int columnIndex) {
    return getFieldName(columnIndex);
  }

  public abstract <V extends DataObject> V getRecord(final int row);

  public List<LayerDataObject> getRecords(final int[] rows) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (final int row : rows) {
      final LayerDataObject record = getRecord(row);
      if (record != null) {
        objects.add(record);
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
    if (columnIndex < attributesOffset) {
      return null;
    } else {
      final DataObject record = getRecord(rowIndex);
      if (record == null) {
        return LOADING_VALUE;
      } else if (record.getState() == DataObjectState.Initalizing) {
        return LOADING_VALUE;
      } else {
        final String name = getFieldName(columnIndex);
        final Object value = record.getValue(name);
        return value;
      }
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final DataObject record = getRecord(rowIndex);
      if (record != null && record.getState() != DataObjectState.Initalizing) {
        final String attributeName = getFieldName(rowIndex, columnIndex);
        if (attributeName != null) {
          if (!isReadOnly(attributeName)) {
            final Class<?> attributeClass = getMetaData().getAttributeClass(
              attributeName);
            if (!Geometry.class.isAssignableFrom(attributeClass)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSelected(boolean selected, final int rowIndex,
    final int columnIndex) {
    final int[] selectedRows = table.getSelectedRows();
    selected = false;
    for (final int selectedRow : selectedRows) {
      if (rowIndex == selectedRow) {
        return true;
      }
    }
    return false;
  }

  public void setAttributeNames(final Collection<String> attributeNames) {
    if (attributeNames == null || attributeNames.isEmpty()) {
      final DataObjectMetaData metaData = getMetaData();
      this.attributeNames = new ArrayList<String>(metaData.getAttributeNames());
    } else {
      this.attributeNames = new ArrayList<String>(attributeNames);
    }
  }

  public void setAttributesOffset(final int attributesOffset) {
    this.attributesOffset = attributesOffset;
  }

  public void setAttributeTitles(final List<String> attributeTitles) {
    this.attributeTitles.clear();
    for (int i = 0; i < this.attributeNames.size(); i++) {
      String title;
      if (i < attributeTitles.size()) {
        title = attributeTitles.get(i);
      } else {
        final String attributeName = getFieldName(i);
        final DataObjectMetaData metaData = getMetaData();
        final Attribute attribute = metaData.getAttribute(attributeName);
        title = attribute.getTitle();
      }
      this.attributeTitles.add(title);
    }
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

  public void setSortOrder(final String idAttributeName) {
    if (StringUtils.hasText(idAttributeName)) {
      final int index = this.attributeNames.indexOf(idAttributeName);
      if (index != -1) {
        setSortOrder(index);
      }
    }
  }

  public void setTable(final DataObjectRowTable table) {
    this.table = table;
    addTableModelListener(table);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {

      if (columnIndex >= attributesOffset) {
        final DataObject object = getRecord(rowIndex);
        if (object != null) {
          final String name = getFieldName(columnIndex);
          final Object objectValue = toObjectValue(columnIndex, value);
          object.setValue(name, objectValue);
        }
      }
    }

  }

  @Override
  public final String toDisplayValue(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
    int rowHeight = table.getRowHeight();
    String displayValue;
    final DataObject record = getRecord(rowIndex);
    if (record == null) {
      rowHeight = 1;
      displayValue = null;
    } else {
      if (record.getState() == DataObjectState.Initalizing) {
        displayValue = LOADING_VALUE;
      } else {
        displayValue = toDisplayValueInternal(rowIndex, attributeIndex,
          objectValue);
      }
    }
    if (rowHeight != table.getRowHeight(rowIndex)) {
      table.setRowHeight(rowIndex, rowHeight);
    }
    return displayValue;
  }

  protected String toDisplayValueInternal(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
    return super.toDisplayValue(rowIndex, attributeIndex, objectValue);
  }
}
