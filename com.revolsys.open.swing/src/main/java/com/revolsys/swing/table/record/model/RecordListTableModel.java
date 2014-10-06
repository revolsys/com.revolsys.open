package com.revolsys.swing.table.record.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.swing.JTable;
import javax.swing.SortOrder;

import com.revolsys.data.comparator.RecordAttributeComparator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.util.Reorderable;

public class RecordListTableModel extends RecordRowTableModel implements
  Reorderable {
  public static TablePanel createPanel(final AbstractRecordLayer layer) {
    return createPanel(layer.getRecordDefinition(), new ArrayList<Record>(),
      layer.getFieldNamesSet());
  }

  public static TablePanel createPanel(final AbstractRecordLayer layer,
    final Collection<? extends Record> records) {
    return createPanel(layer.getRecordDefinition(), records,
      layer.getFieldNames());
  }

  public static TablePanel createPanel(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records,
    final Collection<String> attributeNames) {
    final RecordListTableModel model = new RecordListTableModel(
      recordDefinition, records, attributeNames);
    final JTable table = new RecordRowTable(model);
    return new TablePanel(table);
  }

  public static TablePanel createPanel(final RecordDefinition recordDefinition,
    final List<? extends Record> records, final String... attributeNames) {
    return createPanel(recordDefinition, records, Arrays.asList(attributeNames));
  }

  private static final long serialVersionUID = 1L;

  private final List<Record> records = new ArrayList<>();

  public RecordListTableModel(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records,
    final Collection<String> columnNames) {
    super(recordDefinition, columnNames);
    if (records != null) {
      this.records.addAll(records);
    }
    setEditable(true);
  }

  public void add(final int index, final Record record) {
    this.records.add(index, record);
    fireTableDataChanged();
  }

  public void add(final Record... records) {
    for (final Record record : records) {
      this.records.add(record);
    }
    fireTableDataChanged();
  }

  public void addAll(final Collection<? extends Record> records) {
    this.records.clear();
    this.records.addAll(records);
  }

  public void clear() {
    this.records.clear();
    fireTableDataChanged();
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.records.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int index) {
    if (index >= 0 && index < this.records.size()) {
      return (V)this.records.get(index);
    } else {
      return null;
    }
  }

  /**
   * @return the records
   */
  @SuppressWarnings("unchecked")
  public <V extends Record> List<V> getRecords() {
    return (List<V>)this.records;
  }

  @Override
  public int getRowCount() {
    return this.records.size();
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String attributeName = getFieldName(rowIndex, columnIndex);
      if (isReadOnly(attributeName)) {
        return false;
      } else {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final DataType dataType = recordDefinition.getFieldType(attributeName);
        if (dataType == null) {
          return false;
        } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
          return false;
        } else {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  public void remove(final int... rows) {
    final List<Record> rowsToRemove = getRecords(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<? extends Record> records) {
    for (final Record record : records) {
      final int row = this.records.indexOf(record);
      if (row != -1) {
        this.records.remove(row);
      }
    }
    fireTableDataChanged();
  }

  public void removeAll(final Record... records) {
    removeAll(Arrays.asList(records));
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Record record = getRecord(fromIndex);
    removeAll(record);
    add(toIndex, record);
    clearSortedColumns();
    firePropertyChange("reorder", false, true);
  }

  /**
   * @param records the records to set
   */
  public void setRecords(final Collection<? extends Record> records) {
    this.records.clear();
    if (records != null) {
      this.records.addAll(records);
    }
    fireTableDataChanged();
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    if (this.records != null) {
      final String attributeName = getFieldName(column);
      final Comparator<Record> comparitor = new RecordAttributeComparator(
        sortOrder == SortOrder.ASCENDING, attributeName);
      Collections.sort(this.records, comparitor);
      fireTableDataChanged();
    }
    return sortOrder;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      final PropertyChangeEvent event = new PropertyChangeEvent(record, name,
        oldValue, value);
      getPropertyChangeSupport().firePropertyChange(event);
    }
  }

}
