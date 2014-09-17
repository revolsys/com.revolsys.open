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
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public abstract class RecordRowTableModel extends AbstractRecordTableModel
implements SortableTableModel, CellEditorListener {

  public static final String LOADING_VALUE = "\u2026";

  private static final long serialVersionUID = 1L;

  private List<String> attributeNames = new ArrayList<String>();

  private final List<String> attributeTitles = new ArrayList<String>();

  private Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<Integer, SortOrder>();

  private RecordRowTable table;

  /** The columnIndex that the attribute start. Allows for extra columns in subclasses.*/
  private int attributesOffset;

  public RecordRowTableModel(final RecordDefinition recordDefinition,
    final Collection<String> attributeNames) {
    super(recordDefinition);
    setAttributeNames(attributeNames);
    setAttributeTitles(Collections.<String> emptyList());
    final String idAttributeName = recordDefinition.getIdAttributeName();
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
    return this.attributesOffset;
  }

  public List<String> getAttributeTitles() {
    return this.attributeTitles;
  }

  public Attribute getColumnAttribute(final int columnIndex) {
    if (columnIndex < this.attributesOffset) {
      return null;
    } else {
      final String name = getFieldName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      return recordDefinition.getAttribute(name);
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex < this.attributesOffset) {
      return Object.class;
    } else {
      final String name = getFieldName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      final DataType type = recordDefinition.getAttributeType(name);
      if (type == null) {
        return Object.class;
      } else {
        return type.getJavaClass();
      }
    }
  }

  @Override
  public int getColumnCount() {
    final int numColumns = this.attributesOffset + this.attributeNames.size();
    return numColumns;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex < this.attributesOffset) {
      return null;
    } else {
      return this.attributeTitles.get(columnIndex - this.attributesOffset);
    }
  }

  @Override
  public String getFieldName(final int columnIndex) {
    if (columnIndex < this.attributesOffset) {
      return null;
    } else {
      final int fieldIndex = columnIndex - this.attributesOffset;
      if (fieldIndex < this.attributeNames.size()) {
        final String attributeName = this.attributeNames.get(fieldIndex);
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
      } else {
        return null;
      }
    }
  }

  @Override
  public String getFieldName(final int rowIndex, final int columnIndex) {
    return getFieldName(columnIndex);
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
    if (columnIndex < this.attributesOffset) {
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
          final String attributeName = getFieldName(rowIndex, columnIndex);
          if (attributeName != null) {
            if (!isReadOnly(attributeName)) {
              final RecordDefinition recordDefinition = getRecordDefinition();
              final Class<?> attributeClass = recordDefinition.getAttributeClass(attributeName);
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
  public boolean isSelected(boolean selected, final int rowIndex,
    final int columnIndex) {
    final int[] selectedRows = this.table.getSelectedRows();
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
      final RecordDefinition recordDefinition = getRecordDefinition();
      this.attributeNames = new ArrayList<String>(
          recordDefinition.getAttributeNames());
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
        final RecordDefinition recordDefinition = getRecordDefinition();
        final Attribute attribute = recordDefinition.getAttribute(attributeName);
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
    if (Property.hasValue(idAttributeName)) {
      final int index = this.attributeNames.indexOf(idAttributeName);
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
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {

      if (columnIndex >= this.attributesOffset) {
        final Record object = getRecord(rowIndex);
        if (object != null) {
          final String name = getFieldName(columnIndex);
          final Object objectValue = toObjectValue(columnIndex, value);
          object.setValue(name, objectValue);
        }
      }
    }

  }

  @Override
  public String toCopyValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    String text;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String idFieldName = recordDefinition.getIdAttributeName();
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
        codeTable = recordDefinition.getCodeTableByColumn(name);
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

  @Override
  public final String toDisplayValue(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
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
        displayValue = toDisplayValueInternal(rowIndex, attributeIndex,
          objectValue);
      }
    }
    if (rowHeight != this.table.getRowHeight(rowIndex)) {
      this.table.setRowHeight(rowIndex, rowHeight);
    }
    return displayValue;
  }

  protected String toDisplayValueInternal(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
    return super.toDisplayValue(rowIndex, attributeIndex, objectValue);
  }
}
