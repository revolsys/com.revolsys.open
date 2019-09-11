package com.revolsys.swing.table.lambda.column;

import java.util.List;

public class RowValuesTableModelColumn<V> extends AbstractTableModelColumn {

  private final List<V> values;

  public RowValuesTableModelColumn(final String columnName, final Class<? super V> columnClass,
    final List<V> values) {
    super(columnName, columnClass, false);
    this.values = values;
  }

  @Override
  public Object getValueAt(final int rowIndex) {
    if (rowIndex >= 0 && rowIndex < this.values.size()) {
      return this.values.get(rowIndex);
    } else {
      return null;
    }
  }
}
