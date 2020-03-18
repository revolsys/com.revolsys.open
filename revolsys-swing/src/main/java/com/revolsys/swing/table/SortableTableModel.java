package com.revolsys.swing.table;

import javax.swing.SortOrder;
import javax.swing.table.TableModel;

public interface SortableTableModel extends TableModel {
  SortOrder getSortOrder(int column);

  /**
   * Set the model to sort on the column. If the column is already sorted the
   * sort order will be reversed, otherwise it will be set to ascending.
   *
   * @param column The column to set as sorted.
   * @return The new sort order.
   */
  SortOrder setSortOrder(int column);
}
