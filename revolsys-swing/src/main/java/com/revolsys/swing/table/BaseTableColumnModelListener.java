package com.revolsys.swing.table;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

public interface BaseTableColumnModelListener extends TableColumnModelListener {
  @Override
  default void columnAdded(final TableColumnModelEvent e) {
  }

  @Override
  default void columnMarginChanged(final ChangeEvent e) {
  }

  @Override
  default void columnMoved(final TableColumnModelEvent e) {
  }

  @Override
  default void columnRemoved(final TableColumnModelEvent e) {
  }

  @Override
  default void columnSelectionChanged(final ListSelectionEvent e) {
  }
}
