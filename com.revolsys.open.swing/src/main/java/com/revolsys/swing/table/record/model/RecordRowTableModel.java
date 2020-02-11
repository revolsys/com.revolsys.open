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
import javax.swing.SortOrder;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public abstract class RecordRowTableModel extends AbstractRecordTableModel
  implements SortableTableModel {
  public static final String LOADING_VALUE = "\u2026";

  private static final long serialVersionUID = 1L;

  private List<String> fieldNames = Collections.emptyList();

  private List<FieldDefinition> fields = Collections.emptyList();

  /** The columnIndex that the fields start. Allows for extra columns in subclasses.*/
  private int fieldsOffset;

  private final List<String> fieldTitles = new ArrayList<>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<>();

  public RecordRowTableModel(final RecordDefinition recordDefinition) {
    this(recordDefinition, Collections.<String> emptyList());
  }

  public RecordRowTableModel(final RecordDefinition recordDefinition,
    final Collection<String> fieldNames) {
    this(recordDefinition, fieldNames, 0);
  }

  public RecordRowTableModel(final RecordDefinition recordDefinition,
    final Collection<String> fieldNames, final int fieldsOffset) {
    super(recordDefinition);
    if (Property.hasValue(fieldNames)) {
      setFieldNamesAndTitles(fieldNames, Collections.<String> emptyList());
    }
    this.fieldsOffset = fieldsOffset;
    if (recordDefinition != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      setSortOrder(idFieldName);
    }
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Consumer<V> consumer) {
    return addMenuItem(groupName, name, iconName, (Predicate<V>)null, consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enabledFilter,
    final Consumer<V> consumer) {
    final MenuFactory menu = getMenu();
    return LayerRecordMenu.addMenuItem(menu, groupName, -1, name, null, iconName, enabledFilter,
      consumer);
  }

  public <V extends Record> RunnableAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String toolTip, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer) {
    final MenuFactory menu = getMenu();
    return LayerRecordMenu.addMenuItem(menu, groupName, index, name, toolTip, iconName,
      enabledFilter, consumer);
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
  public Class<?> getColumnClass(final int columnIndex) {
    final FieldDefinition fieldDefinition = getColumnFieldDefinition(columnIndex);
    if (fieldDefinition == null) {
      return Object.class;
    } else {
      return fieldDefinition.getTypeClass();
    }
  }

  @Override
  public int getColumnCount() {
    final int numColumns = this.fieldsOffset + this.fieldNames.size();
    return numColumns;
  }

  public FieldDefinition getColumnFieldDefinition(final int columnIndex) {
    if (columnIndex >= this.fieldsOffset) {
      final int fieldIndex = columnIndex - this.fieldsOffset;
      if (fieldIndex < this.fields.size()) {
        return this.fields.get(fieldIndex);
      }
    }
    return null;
  }

  public int getColumnFieldIndex(final String fieldName) {
    return this.fieldNames.indexOf(fieldName) + this.fieldsOffset;
  }

  @Override
  public String getColumnFieldName(final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final int fieldIndex = columnIndex - this.fieldsOffset;
      if (fieldIndex < this.fieldNames.size()) {
        final String fieldName = this.fieldNames.get(fieldIndex);
        if (fieldName == null) {
          return null;
        } else {
          if (this.fields.get(fieldIndex) == null) {
            return fieldName;
          } else {
            final int index = fieldName.indexOf('.');
            if (index == -1) {
              return fieldName;
            } else {
              return fieldName.substring(0, index);
            }
          }
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public String getColumnFieldName(final int rowIndex, final int columnIndex) {
    return getColumnFieldName(columnIndex);
  }

  public int getColumnFieldsOffset() {
    return this.fieldsOffset;
  }

  public List<String> getColumnFieldTitles() {
    return this.fieldTitles;
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

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public Object getPrototypeValue(final int columnIndex) {
    FieldDefinition fieldDefinition = getColumnFieldDefinition(columnIndex);
    if (fieldDefinition == null) {
      return null;
    } else {
      final CodeTable codeTable = fieldDefinition.getCodeTable();
      if (codeTable != null) {
        final FieldDefinition valueFieldDefinition = codeTable.getValueFieldDefinition();
        if (valueFieldDefinition != null) {
          fieldDefinition = valueFieldDefinition;
        } else {
          Object maxValue = "";
          int maxLength = 0;
          for (final Object value : codeTable.getValues()) {
            if (value != null) {
              final int length = DataTypes.toString(value).length();
              if (length > maxLength) {
                maxValue = value;
                maxLength = length;
              }
            }
          }
          return maxValue;
        }
      }
      final DataType fieldType = fieldDefinition.getDataType();
      final Class<?> fieldClass = fieldDefinition.getTypeClass();
      final int fieldLength = fieldDefinition.getLength();
      if (DataTypes.BOOLEAN.equals(fieldType)) {
        return Byte.MIN_VALUE;
      } else if (DataTypes.BYTE.equals(fieldType)) {
        return Byte.MIN_VALUE;
      } else if (DataTypes.SHORT.equals(fieldType)) {
        return Short.MAX_VALUE;
      } else if (DataTypes.INT.equals(fieldType)) {
        return Integer.MAX_VALUE;
      } else if (DataTypes.LONG.equals(fieldType)) {
        return Long.MAX_VALUE;
      } else if (DataTypes.SQL_DATE.equals(fieldType)) {
        return Dates.getSqlDate("9999-12-31");
      } else if (DataTypes.DATE.equals(fieldType)) {
        return Dates.getDate("9999-12-29 23:59:59.999");
      } else if (DataTypes.DATE_TIME.equals(fieldType)) {
        return Dates.getTimestamp("9999-12-29 23:59:59.999");
      } else if (DataTypes.TIMESTAMP.equals(fieldType)) {
        return Dates.getTimestamp("9999-12-29 23:59:59.999");
      } else if (Geometry.class.isAssignableFrom(fieldClass)) {
        return fieldType.getName();
      } else {
        if (fieldLength < 1 || fieldLength > 30) {
          return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        } else {
          final StringBuilder string = new StringBuilder();
          for (int i = 0; i < fieldLength; i++) {
            string.append("X");
          }
          return string.toString();
        }
      }
      // if (Number.class.isAssignableFrom(fieldClass)) {
      // final StringBuilder numberString = new StringBuilder();
      // final Object maxValue = fieldDefinition.getMaxValue();
      // final Object minValue = fieldDefinition.getMinValue();
      // if (maxValue == null) {
      // for (int i = 0; i <= maxLength; i++) {
      // numberString.append("9");
      // }
      // return fieldType.toObject(numberString.toString());
      // } else {
      // if (minValue instanceof Number) {
      // numberString.append(((Number)minValue).longValue());
      // }
      // }
      // if (Float.class.isAssignableFrom(fieldClass) ||
      // Double.class.isAssignableFrom(fieldClass)
      // || BigDecimal.class.isAssignableFrom(fieldClass)) {
      // numberString.append('.');
      // final int fieldScale = fieldDefinition.getScale();
      // for (int i = 0; i <= fieldScale; i++) {
      // numberString.append("9");
      // }
      // final BigDecimal scaleValue = new BigDecimal(numberString.toString());
      // }
      // }
      // return super.getPrototypeValue(columnIndex);
    }
  }

  public abstract <V extends Record> V getRecord(final int row);

  public <V extends Record> List<V> getRecords(final int[] rows) {
    final List<V> records = new ArrayList<>();
    for (final int row : rows) {
      final V record = getRecord(row);
      if (record != null) {
        records.add(record);
      }
    }
    return records;
  }

  protected Object getRecordValue(final int columnIndex, final Record record, final String name) {
    return record.getValue(name);
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

  @Override
  public RecordRowTable getTable() {
    return (RecordRowTable)super.getTable();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex < this.fieldsOffset) {
      return null;
    } else {
      final Record record = getRecord(rowIndex);
      if (record == null) {
        return LOADING_VALUE;
      } else if (record.getState() == RecordState.INITIALIZING) {
        return LOADING_VALUE;
      } else {
        final String name = getColumnFieldName(columnIndex);
        return getRecordValue(columnIndex, record, name);
      }
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final Record record = getRecord(rowIndex);
      if (record != null) {
        return isCellEditable(rowIndex, columnIndex, record);
      }
    }
    return false;
  }

  protected boolean isCellEditable(final int rowIndex, final int columnIndex, final Record record) {
    final RecordState state = record.getState();
    if (state != RecordState.INITIALIZING && state != RecordState.DELETED) {
      final String fieldName = getColumnFieldName(rowIndex, columnIndex);
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
    return false;
  }

  public boolean isFieldEditable(final int columnIndex) {
    final String fieldName = getColumnFieldName(columnIndex);
    if (fieldName != null) {
      if (!isReadOnly(fieldName)) {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final Class<?> fieldClass = recordDefinition.getFieldClass(fieldName);
        if (!Geometry.class.isAssignableFrom(fieldClass)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isIdField(final int columnIndex) {
    final String fieldName = getColumnFieldName(columnIndex);
    if (fieldName != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      return recordDefinition.isIdField(fieldName);
    }
    return false;
  }

  @Override
  public boolean isSelected(boolean selected, final int rowIndex, final int columnIndex) {
    final RecordRowTable table = getTable();
    final int[] selectedRows = table.getSelectedRows();
    selected = false;
    for (final int selectedRow : selectedRows) {
      if (rowIndex == selectedRow) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isShowCodeValues() {
    return getTable().isShowDisplayValues();
  }

  public boolean isSorted(final int columnIndex) {
    synchronized (this.sortedColumns) {
      return this.sortedColumns.containsKey(columnIndex);
    }
  }

  public void setFieldNames(Collection<String> fieldNames) {
    if (fieldNames == null || fieldNames.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      fieldNames = recordDefinition.getFieldNames();
    }
    final List<String> fieldNamesNew = new ArrayList<>();
    final List<FieldDefinition> fields = new ArrayList<>();
    final RecordDefinition recordDefinition = getRecordDefinition();
    for (final String fieldName : fieldNames) {
      final FieldDefinition fieldDefinition = recordDefinition.getField(fieldName);
      fieldNamesNew.add(fieldName);
      fields.add(fieldDefinition);
    }
    this.fieldNames = fieldNamesNew;
    this.fields = fields;
    fireTableStructureChanged();
  }

  public void setFieldNamesAndTitles(final Collection<String> fieldNames,
    final List<String> fieldTitles) {
    setFieldNames(fieldNames);
    this.fieldTitles.clear();

    for (int i = 0; i < this.fieldNames.size(); i++) {
      final FieldDefinition fieldDefinition = this.fields.get(i);
      this.fields.add(fieldDefinition);
      String title;
      if (i < fieldTitles.size()) {
        title = fieldTitles.get(i);
      } else {
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
    final List<FieldDefinition> fields = this.fields;
    for (int i = 0; i < fields.size(); i++) {
      String title;
      if (i < fieldTitles.size()) {
        title = fieldTitles.get(i);
      } else {
        final FieldDefinition fieldDefinition = fields.get(i);
        title = fieldDefinition.getTitle();
      }
      this.fieldTitles.add(title);
    }
    fireTableStructureChanged();
  }

  protected void setRecordValue(final Record record, final String fieldName, final Object value) {
    final Object objectValue = toObjectValue(fieldName, value);
    setRecordValueDo(record, fieldName, objectValue);
  }

  protected void setRecordValueDo(final Record record, final String fieldName,
    final Object objectValue) {
    record.setValue(fieldName, objectValue);
  }

  public void setSortedColumns(final Map<Integer, SortOrder> sortedColumns) {
    this.sortedColumns = new LinkedHashMap<>();
    if (sortedColumns != null) {
      this.sortedColumns.putAll(sortedColumns);
    }

  }

  @Override
  public SortOrder setSortOrder(final int columnIndex) {
    if (isColumnSortable(columnIndex)) {
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
    } else {
      return SortOrder.UNSORTED;
    }
  }

  public SortOrder setSortOrder(final int columnIndex, final SortOrder sortOrder) {
    if (isColumnSortable(columnIndex)) {
      synchronized (this.sortedColumns) {
        this.sortedColumns.clear();
        this.sortedColumns.put(columnIndex, sortOrder);

        fireTableDataChanged();
        return sortOrder;
      }
    } else {
      return SortOrder.UNSORTED;
    }
  }

  // TODO initial sort order for session layers doesn't always work
  public SortOrder setSortOrder(final String fieldName) {
    int index = 0;
    if (Property.hasValue(fieldName)) {
      index = this.fieldNames.indexOf(fieldName);
      if (index == -1) {
        index = 0;
      }
    }
    final FieldDefinition fieldDefinition = getColumnFieldDefinition(index);
    if (fieldDefinition != null) {
      final Class<?> fieldClass = fieldDefinition.getTypeClass();
      if (Geometry.class.isAssignableFrom(fieldClass)) {
        return SortOrder.ASCENDING;
      }
    }
    return setSortOrder(index + this.fieldsOffset, SortOrder.ASCENDING);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {
      if (columnIndex >= this.fieldsOffset) {
        final Record record = getRecord(rowIndex);
        if (record != null) {
          final String fieldName = getColumnFieldName(columnIndex);
          setRecordValue(record, fieldName, value);
        }
      }
    }
  }

  @Override
  public String toCopyValue(final int rowIndex, int fieldIndex, final Object recordValue) {
    if (fieldIndex < this.fieldsOffset) {
      return DataTypes.toString(recordValue);
    } else {
      fieldIndex -= this.fieldsOffset;
      String text;
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String fieldName = getColumnFieldName(fieldIndex);
      if (recordValue == null) {
        return null;
      } else {
        if (recordValue instanceof Geometry) {
          final Geometry geometry = (Geometry)recordValue;
          return geometry.toString();
        }
        CodeTable codeTable = null;
        if (!recordDefinition.isIdField(fieldName)) {
          codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
        }
        if (codeTable == null) {
          text = DataTypes.toString(recordValue);
        } else {
          final List<Object> values = codeTable.getValues(Identifier.newIdentifier(recordValue));
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
    final Record record = getRecord(rowIndex);
    if (record == null) {
      return null;
    } else {
      if (record.getState() == RecordState.INITIALIZING) {
        return LOADING_VALUE;
      } else {
        return toDisplayValueInternal(rowIndex, fieldIndex, objectValue);
      }
    }
  }

  protected String toDisplayValueInternal(final int rowIndex, final int fieldIndex,
    final Object objectValue) {
    return super.toDisplayValue(rowIndex, fieldIndex, objectValue);
  }
}
