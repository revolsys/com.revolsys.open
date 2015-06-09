package com.revolsys.swing.table.record.model;

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

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public abstract class RecordRowTableModel extends AbstractRecordTableModel implements
  SortableTableModel, CellEditorListener {

  public static final String LOADING_VALUE = "\u2026";

  private static final long serialVersionUID = 1L;

  private List<String> fieldNames = new ArrayList<>();

  /** The columnIndex that the fields start. Allows for extra columns in subclasses.*/
  private int fieldsOffset;

  private final List<String> fieldTitles = new ArrayList<>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<>();

  private RecordRowTable table;

  public RecordRowTableModel(final RecordDefinition recordDefinition) {
    this(recordDefinition, Collections.<String> emptyList());
  }

  public RecordRowTableModel(final RecordDefinition recordDefinition,
    final Collection<String> fieldNames) {
    super(recordDefinition);
    if (Property.hasValue(fieldNames)) {
      setFieldNamesAndTitles(fieldNames, Collections.<String> emptyList());
    }
    final String idFieldName = recordDefinition.getIdFieldName();
    setSortOrder(idFieldName);
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
    if (this.table != null) {
      removeTableModelListener(this.table);
      this.table = null;
    }
    super.dispose();
    this.sortedColumns = null;
  }

  @Override
  public void editingCanceled(final ChangeEvent event) {
  }

  @Override
  public void editingStopped(final ChangeEvent event) {
  }

  public FieldDefinition getColumnAttribute(final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final String name = getFieldName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      return recordDefinition.getField(name);
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return Object.class;
    } else {
      final String name = getFieldName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      final DataType type = recordDefinition.getFieldType(name);
      if (type == null) {
        return Object.class;
      } else {
        return type.getJavaClass();
      }
    }
  }

  @Override
  public int getColumnCount() {
    final int numColumns = this.fieldsOffset + this.fieldNames.size();
    return numColumns;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex >= this.fieldsOffset) {
      final int fieldIndex = columnIndex - this.fieldsOffset;
      if (fieldIndex < this.fieldTitles.size()) {
        return this.fieldTitles.get(fieldIndex);
      }
    }
    return null;
  }

  public int getFieldIndex(final String fieldName) {
    return this.fieldNames.indexOf(fieldName);
  }

  @Override
  public String getFieldName(final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final int fieldIndex = columnIndex - this.fieldsOffset;
      if (fieldIndex < this.fieldNames.size()) {
        final String fieldName = this.fieldNames.get(fieldIndex);
        if (fieldName == null) {
          return null;
        } else {
          final int index = fieldName.indexOf('.');
          if (index == -1) {
            return fieldName;
          } else {
            return fieldName.substring(0, index);
          }
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public String getFieldName(final int rowIndex, final int columnIndex) {
    return getFieldName(columnIndex);
  }

  public int getFieldsOffset() {
    return this.fieldsOffset;
  }

  public List<String> getFieldTitles() {
    return this.fieldTitles;
  }

  public abstract <V extends Record> V getRecord(final int row);

  public <V extends Record> List<V> getRecords(final int[] rows) {
    final List<V> records = new ArrayList<V>();
    for (final int row : rows) {
      final V record = getRecord(row);
      if (record != null) {
        records.add(record);
      }
    }
    return records;
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

  public RecordRowTable getTable() {
    return this.table;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final Record record = getRecord(rowIndex);
      if (record == null) {
        return LOADING_VALUE;
      } else if (record.getState() == RecordState.Initalizing) {
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
      final Record record = getRecord(rowIndex);
      if (record != null) {
        final RecordState state = record.getState();
        if (state != RecordState.Initalizing && state != RecordState.Deleted) {
          final String fieldName = getFieldName(rowIndex, columnIndex);
          if (fieldName != null) {
            if (!isReadOnly(fieldName)) {
              final RecordDefinition recordDefinition = getRecordDefinition();
              final Class<?> attributeClass = recordDefinition.getFieldClass(fieldName);
              if (!Geometry.class.isAssignableFrom(attributeClass)) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSelected(boolean selected, final int rowIndex, final int columnIndex) {
    final int[] selectedRows = this.table.getSelectedRows();
    selected = false;
    for (final int selectedRow : selectedRows) {
      if (rowIndex == selectedRow) {
        return true;
      }
    }
    return false;
  }

  public void setFieldNames(final Collection<String> fieldNames) {
    if (fieldNames == null || fieldNames.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      this.fieldNames = new ArrayList<>(recordDefinition.getFieldNames());
    } else {
      this.fieldNames = new ArrayList<>(fieldNames);
    }
    fireTableStructureChanged();
  }

  public void setFieldNamesAndTitles(final Collection<String> fieldNames,
    final List<String> fieldTitles) {
    if (fieldNames == null || fieldNames.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      this.fieldNames = new ArrayList<>(recordDefinition.getFieldNames());
    } else {
      this.fieldNames = new ArrayList<>(fieldNames);
    }
    this.fieldTitles.clear();
    for (int i = 0; i < this.fieldNames.size(); i++) {
      String title;
      if (i < fieldTitles.size()) {
        title = fieldTitles.get(i);
      } else {
        final String fieldName = getFieldName(i);
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition attribute = recordDefinition.getField(fieldName);
        title = attribute.getTitle();
      }
      this.fieldTitles.add(title);
    }
    fireTableStructureChanged();
  }

  public void setFieldsOffset(final int attributesOffset) {
    this.fieldsOffset = attributesOffset;
  }

  public void setFieldTitles(final List<String> fieldTitles) {
    this.fieldTitles.clear();
    for (int i = 0; i < this.fieldNames.size(); i++) {
      String title;
      if (i < fieldTitles.size()) {
        title = fieldTitles.get(i);
      } else {
        final String fieldName = getFieldName(i);
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition attribute = recordDefinition.getField(fieldName);
        title = attribute.getTitle();
      }
      this.fieldTitles.add(title);
    }
    fireTableStructureChanged();
  }

  public void setSortedColumns(final Map<Integer, SortOrder> sortedColumns) {
    this.sortedColumns = new LinkedHashMap<>();
    if (sortedColumns != null) {
      this.sortedColumns.putAll(sortedColumns);
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

  public void setSortOrder(final String idFieldName) {
    if (Property.hasValue(idFieldName)) {
      final int index = this.fieldNames.indexOf(idFieldName);
      if (index != -1) {
        setSortOrder(index);
      }
    }
  }

  public void setTable(final RecordRowTable table) {
    this.table = table;
    addTableModelListener(table);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {

      if (columnIndex >= this.fieldsOffset) {
        final Record record = getRecord(rowIndex);
        if (record != null) {
          final String fieldName = getFieldName(columnIndex);
          final Object objectValue = toObjectValue(fieldName, value);
          record.setValue(fieldName, objectValue);
        }
      }
    }

  }

  @Override
  public String toCopyValue(final int rowIndex, int attributeIndex, final Object objectValue) {
    if (attributeIndex < this.fieldsOffset) {
      return StringConverterRegistry.toString(objectValue);
    } else {
      attributeIndex -= this.fieldsOffset;
      String text;
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String idFieldName = recordDefinition.getIdFieldName();
      final String name = getFieldName(attributeIndex);
      if (objectValue == null) {
        return null;
      } else {
        if (objectValue instanceof Geometry) {
          final Geometry geometry = (Geometry)objectValue;
          return geometry.toString();
        }
        CodeTable codeTable = null;
        if (!name.equals(idFieldName)) {
          codeTable = recordDefinition.getCodeTableByFieldName(name);
        }
        if (codeTable == null) {
          text = StringConverterRegistry.toString(objectValue);
        } else {
          final List<Object> values = codeTable.getValues(SingleIdentifier.create(objectValue));
          if (values == null || values.isEmpty()) {
            return null;
          } else {
            text = CollectionUtil.toString(values);
          }
        }
        if (text.length() == 0) {
          return null;
        }
      }
      return text;
    }
  }

  @Override
  public final String toDisplayValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    int rowHeight = this.table.getRowHeight();
    String displayValue;
    final Record record = getRecord(rowIndex);
    if (record == null) {
      rowHeight = 1;
      displayValue = null;
    } else {
      if (record.getState() == RecordState.Initalizing) {
        displayValue = LOADING_VALUE;
      } else {
        displayValue = toDisplayValueInternal(rowIndex, attributeIndex, objectValue);
      }
    }
    if (rowHeight != this.table.getRowHeight(rowIndex)) {
      this.table.setRowHeight(rowIndex, rowHeight);
    }
    return displayValue;
  }

  protected String toDisplayValueInternal(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    return super.toDisplayValue(rowIndex, attributeIndex, objectValue);
  }
}
