package com.revolsys.swing.map.layer.dataobject.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;

import com.revolsys.comparator.ObjectPropertyComparator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectMetaDataMapListTableModel extends AbstractTableModel
  implements Reorderable, SortableTableModel {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final DataObjectLayer layer) {
    return createPanel(layer.getMetaData(),
      new ArrayList<Map<String, Object>>(), layer.getColumnNames());
  }

  public static TablePanel createPanel(final DataObjectLayer layer,
    final List<Map<String, Object>> objects) {
    return createPanel(layer.getMetaData(), objects, layer.getColumnNames());
  }

  public static TablePanel createPanel(final DataObjectMetaData metaData,
    final Collection<Map<String, Object>> objects,
    final Collection<String> attributeNames) {
    final DataObjectMetaDataMapListTableModel model = new DataObjectMetaDataMapListTableModel(
      metaData, objects, attributeNames);
    final JTable table = new BaseJxTable(model);
    return new TablePanel(table);
  }

  public static TablePanel createPanel(final DataObjectMetaData metaData,
    final List<Map<String, Object>> objects, final String... attributeNames) {
    return createPanel(metaData, objects, Arrays.asList(attributeNames));
  }

  private List<String> attributeNames = new ArrayList<String>();

  private final List<String> attributeTitles = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private DataObjectMetaData metaData;

  private boolean editable;

  private BaseJxTable table;

  private final List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectMetaDataMapListTableModel(final DataObjectMetaData metaData,
    final Collection<Map<String, Object>> objects,
    final Collection<String> columnNames) {
    this.metaData = metaData;
    setAttributeNames(this.attributeNames);
    setAttributeTitles(Collections.<String> emptyList());
    if (objects != null) {
      this.objects.addAll(objects);
    }
    setEditable(true);
  }

  public void add(final int index, final Map<String, Object> object) {
    this.objects.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

  public void add(final Map<String, Object>... objects) {
    for (final Map<String, Object> object : objects) {
      this.objects.add(object);
      fireTableRowsInserted(this.objects.size() - 1, this.objects.size());
    }
  }

  public void addAll(final Collection<Map<String, Object>> objects) {
    this.objects.clear();
    this.objects.addAll(objects);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  public void clear() {
    this.objects.clear();
    fireTableDataChanged();
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

  private void firePropertyChange(final Map<String, Object> object,
    final String name, final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : this.propertyChangeListeners) {
      listener.propertyChange(event);
    }
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

  public Map<String, Object> getObject(final int index) {
    return this.objects.get(index);
  }

  /**
   * @return the objects
   */
  public List<Map<String, Object>> getObjects() {
    return this.objects;
  }

  public List<Map<String, Object>> getObjects(final int[] rows) {
    final List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
    for (final int row : rows) {
      final Map<String, Object> object = getObject(row);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(this.propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return this.objects.size();
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
    final Map<String, Object> object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      final String name = getAttributeName(columnIndex);
      return object.get(name);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
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

  public void remove(final int... rows) {
    final List<Map<String, Object>> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<Map<String, Object>> objects) {
    for (final Map<String, Object> object : objects) {
      final int row = this.objects.indexOf(object);
      if (row != -1) {
        this.objects.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removeAll(final Map<String, Object>... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Map<String, Object> object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
    clearSortedColumns();
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

  /**
   * @param objects the objects to set
   */
  public void setObjects(final List<Map<String, Object>> objects) {
    this.objects.clear();
    if (objects != null) {
      this.objects.addAll(objects);
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
      firePropertyChange(object, name, oldValue, value);
    }
  }

}
