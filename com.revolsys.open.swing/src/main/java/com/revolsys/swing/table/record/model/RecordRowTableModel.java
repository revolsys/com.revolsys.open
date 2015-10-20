package com.revolsys.swing.table.record.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PreDestroy;
import javax.swing.Icon;
import javax.swing.SortOrder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public abstract class RecordRowTableModel extends AbstractRecordTableModel
  implements SortableTableModel, CellEditorListener {

  public static final String LOADING_VALUE = "\u2026";

  private static final long serialVersionUID = 1L;

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final Consumer<V> consumer) {
    return addMenuItem(menu, groupName, -1, name, null, iconName, enableCheck, consumer);
  }

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final int index, final CharSequence name, final String toolTip,
    final String iconName, final EnableCheck enableCheck, final Consumer<V> consumer) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = menu.createMenuItem(name, toolTip, icon, enableCheck, () -> {
      final V record = RecordRowTable.getEventRecord();
      if (record != null && consumer != null) {
        consumer.accept(record);
      }
    });
    menu.addComponentFactory(groupName, index, action);
    return action;
  }

  public static <V extends Record> RunnableAction addMenuItem(final MenuFactory menu,
    final String groupName, final int index, final CharSequence name, final String toolTip,
    final String iconName, final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    final EnableCheck enableCheck = RecordRowTable.enableCheck(enabledFilter);
    return addMenuItem(menu, groupName, index, name, toolTip, iconName, enableCheck, consumer);
  }

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

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Consumer<V> consumer) {
    return addMenuItem(groupName, name, iconName, (Predicate<V>)null, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enabledFilter,
    final Consumer<V> consumer) {
    final MenuFactory menu = getMenu();
    return addMenuItem(menu, groupName, -1, name, null, iconName, enabledFilter, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String toolTip, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    final MenuFactory menu = getMenu();
    return addMenuItem(menu, groupName, index, name, toolTip, iconName, enabledFilter, consumer);
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

  public FieldDefinition getColumnFieldDefinition(final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final String name = getFieldName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      return recordDefinition.getField(name);
    }
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
              final Class<?> fieldClass = recordDefinition.getFieldClass(fieldName);
              if (!Geometry.class.isAssignableFrom(fieldClass)) {
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
        final FieldDefinition fieldDefinition = recordDefinition.getField(fieldName);
        title = fieldDefinition.getTitle();
      }
      this.fieldTitles.add(title);
    }
    fireTableStructureChanged();
  }

  public void setFieldsOffset(final int fieldsOffset) {
    this.fieldsOffset = fieldsOffset;
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
        final FieldDefinition fieldDefinition = recordDefinition.getField(fieldName);
        title = fieldDefinition.getTitle();
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
  public String toCopyValue(final int rowIndex, int fieldIndex, final Object recordValue) {
    if (fieldIndex < this.fieldsOffset) {
      return StringConverterRegistry.toString(recordValue);
    } else {
      fieldIndex -= this.fieldsOffset;
      String text;
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String idFieldName = recordDefinition.getIdFieldName();
      final String name = getFieldName(fieldIndex);
      if (recordValue == null) {
        return null;
      } else {
        if (recordValue instanceof Geometry) {
          final Geometry geometry = (Geometry)recordValue;
          return geometry.toString();
        }
        CodeTable codeTable = null;
        if (!name.equals(idFieldName)) {
          codeTable = recordDefinition.getCodeTableByFieldName(name);
        }
        if (codeTable == null) {
          text = StringConverterRegistry.toString(recordValue);
        } else {
          final List<Object> values = codeTable.getValues(Identifier.create(recordValue));
          if (values == null || values.isEmpty()) {
            return null;
          } else {
            text = Strings.toString(values);
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
  public final String toDisplayValue(final int rowIndex, final int fieldIndex,
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
        displayValue = toDisplayValueInternal(rowIndex, fieldIndex, objectValue);
      }
    }
    if (rowHeight != this.table.getRowHeight(rowIndex)) {
      this.table.setRowHeight(rowIndex, rowHeight);
    }
    return displayValue;
  }

  protected String toDisplayValueInternal(final int rowIndex, final int fieldIndex,
    final Object objectValue) {
    return super.toDisplayValue(rowIndex, fieldIndex, objectValue);
  }
}
