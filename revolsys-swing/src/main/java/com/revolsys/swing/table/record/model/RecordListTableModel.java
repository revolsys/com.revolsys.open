package com.revolsys.swing.table.record.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.swing.SortOrder;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.comparator.RecordFieldComparator;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.util.Reorderable;

public class RecordListTableModel extends RecordRowTableModel implements Reorderable {
  private static final long serialVersionUID = 1L;

  public static TablePanel newPanel(final AbstractRecordLayer layer) {
    return newPanel(layer.getRecordDefinition(), new ArrayList<Record>(), layer.getFieldNamesSet());
  }

  public static TablePanel newPanel(final AbstractRecordLayer layer,
    final Collection<? extends Record> records) {
    return newPanel(layer.getRecordDefinition(), records, layer.getFieldNames());
  }

  public static TablePanel newPanel(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records, final Collection<String> fieldNames) {
    final RecordListTableModel model = new RecordListTableModel(recordDefinition, records,
      fieldNames);
    final BaseJTable table = new RecordRowTable(model);
    return new TablePanel(table);
  }

  public static TablePanel newPanel(final RecordDefinition recordDefinition,
    final List<? extends Record> records, final String... fieldNames) {
    return newPanel(recordDefinition, records, Arrays.asList(fieldNames));
  }

  private final List<Record> records = new ArrayList<>();

  public RecordListTableModel(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records, final Collection<String> fieldNames) {
    this(recordDefinition, records, fieldNames, 0);
  }

  public RecordListTableModel(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records, final Collection<String> fieldNames,
    final int fieldsOffset) {
    super(recordDefinition, fieldNames, fieldsOffset);
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
      final String fieldName = getColumnFieldName(rowIndex, columnIndex);
      if (isReadOnly(fieldName)) {
        return false;
      } else {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final DataType dataType = recordDefinition.getFieldType(fieldName);
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

  public void removeAll(final List<? extends Record> records) {
    for (final Record record : records) {
      record.removeFrom(this.records);
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
      final String fieldName = getColumnFieldName(column);
      final Comparator<Record> comparator = new RecordFieldComparator(
        sortOrder == SortOrder.ASCENDING, fieldName);
      Collections.sort(this.records, comparator);
      fireTableDataChanged();
    }
    return sortOrder;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnFieldName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}
