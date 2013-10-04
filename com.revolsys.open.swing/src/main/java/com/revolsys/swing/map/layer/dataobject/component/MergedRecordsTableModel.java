package com.revolsys.swing.map.layer.dataobject.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;

import com.revolsys.comparator.ObjectPropertyComparator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.vividsolutions.jts.geom.Geometry;

public class MergedRecordsTableModel extends AbstractTableModel implements
  SortableTableModel {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final DataObjectLayer layer) {
    final MergedRecordsTable table = new MergedRecordsTable(layer);
    return new TablePanel(table);
  }

  private List<String> attributeNames = new ArrayList<String>();

  private final List<String> attributeTitles = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private DataObjectMetaData metaData;

  private boolean editable;

  private BaseJxTable table;

  private List<DataObject> objects = new ArrayList<DataObject>();

  private DataObject mergedObject;

  public MergedRecordsTableModel(final DataObjectLayer layer) {
    this(layer, null, null);
  }

  public MergedRecordsTableModel(final DataObjectLayer layer,
    final DataObject mergedObject, final Collection<LayerDataObject> objects) {
    this.metaData = layer.getMetaData();
    setAttributeNames(metaData.getAttributeNames());
    setAttributeTitles(metaData.getAttributeTitles());
    setObjects(mergedObject, objects);
    setEditable(true);
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
    this.objects.clear();
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

  public DataObject getMergedObject() {
    return mergedObject;
  }

  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public DataObject getObject(final int index) {
    if (index == this.objects.size()) {
      return mergedObject;
    } else {
      return this.objects.get(index);
    }
  }

  /**
   * @return the objects
   */
  public List<DataObject> getObjects() {
    return this.objects;
  }

  @Override
  public int getRowCount() {
    return this.objects.size() + 1;
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

  public BaseJxTable getTable() {
    return this.table;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else if (columnIndex == 0) {
      if (rowIndex == objects.size()) {
        return "Merge";
      } else {
        return rowIndex + 1;
      }
    } else {
      final String name = getAttributeName(columnIndex);
      return object.get(name);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable() && rowIndex == objects.size()) {
      final String columnName = getColumnName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType dataType = metaData.getAttributeType(columnName);
      if (dataType == null) {
        return false;
      } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public boolean isEditable() {
    return this.editable;
  }

  public void setAttributeNames(final Collection<String> attributeNames) {
    this.attributeNames = new ArrayList<String>();
    this.attributeNames.add("Index");

    if (attributeNames == null || attributeNames.isEmpty()) {
      this.attributeNames.addAll(this.metaData.getAttributeNames());
    } else {
      this.attributeNames.addAll(attributeNames);
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
    this.attributeTitles.add(0, "Index");
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setObjects(final DataObject mergedObject,
    final Collection<LayerDataObject> objects) {
    this.mergedObject = mergedObject;
    if (objects == null) {
      this.objects = new ArrayList<DataObject>();
    } else {
      this.objects = new ArrayList<DataObject>(objects);
    }
    fireTableDataChanged();
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    synchronized (this.sortedColumns) {
      SortOrder sortOrder = this.sortedColumns.get(column);
      this.sortedColumns.clear();
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
      this.sortedColumns.put(column, sortOrder);

      final String attributeName = getAttributeName(column);
      final Comparator<Map<String, Object>> comparitor = new ObjectPropertyComparator<Map<String, Object>>(
        sortOrder == SortOrder.ASCENDING, attributeName);
      Collections.sort(this.objects, comparitor);
      fireTableDataChanged();
      return sortOrder;
    }

  }

  public void setTable(final BaseJxTable table) {
    this.table = table;
    addTableModelListener(table);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Map<String, Object> object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.get(name);
      object.put(name, value);
    }
  }

}
