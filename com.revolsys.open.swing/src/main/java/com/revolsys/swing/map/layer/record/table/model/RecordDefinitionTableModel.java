package com.revolsys.swing.map.layer.record.table.model;

import java.util.Arrays;
import java.util.List;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJxTable;

public class RecordDefinitionTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private static final List<String> COLUMN_NAMES = Arrays.asList("#", "Field",
    "Type", "Length", "Scale", "Min", "Max", "Required", "Description");

  private static final List<Class<?>> COLUMN_CLASSES = Arrays.<Class<?>> asList(
    Integer.class, String.class, String.class, Integer.class, Integer.class,
    Object.class, Object.class, Boolean.class, String.class);

  public static BaseJxTable createTable(final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      final RecordDefinitionTableModel model = new RecordDefinitionTableModel(
        recordDefinition);
      final BaseJxTable table = new BaseJxTable(model);
      table.resizeColumnsToContent();
      return table;
    }
  }

  private final RecordDefinition recordDefinition;

  public RecordDefinitionTableModel(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return COLUMN_CLASSES.get(columnIndex);
  }

  @Override
  public int getColumnCount() {
    return 9;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES.get(column);
  }

  @Override
  public int getRowCount() {
    return this.recordDefinition.getFieldCount();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final FieldDefinition attribute = this.recordDefinition.getField(rowIndex);
    if (attribute == null) {
      return "...";
    } else {
      switch (columnIndex) {
        case 0:
          return rowIndex;
        case 1:
          return attribute.getName();
        case 2:
          return attribute.getType();
        case 3:
          return attribute.getLength();
        case 4:
          return attribute.getScale();
        case 5:
          return attribute.getMinValue();
        case 6:
          return attribute.getMaxValue();
        case 7:
          return attribute.isRequired();
        case 8:
          return attribute.getDescription();
        default:
          return "...";
      }
    }
  }
}
