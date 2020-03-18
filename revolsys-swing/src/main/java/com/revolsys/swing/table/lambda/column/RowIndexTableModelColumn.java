package com.revolsys.swing.table.lambda.column;

public class RowIndexTableModelColumn extends AbstractTableModelColumn {

  public RowIndexTableModelColumn() {
    super("#", Integer.class, false);
  }

  @Override
  public Object getValueAt(final int rowIndex) {
    return rowIndex + 1;
  }
}
