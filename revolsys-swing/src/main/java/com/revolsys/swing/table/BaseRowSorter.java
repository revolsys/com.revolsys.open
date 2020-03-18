package com.revolsys.swing.table;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.sort.DefaultSortController;
import org.jdesktop.swingx.sort.TableSortController;
import org.jeometry.common.compare.NumericComparator;

public class BaseRowSorter extends TableSortController<TableModel> {

  public BaseRowSorter() {
  }

  public BaseRowSorter(final TableModel model) {
    super(model);
  }

  @Override
  public Comparator<?> getComparator(final int column) {
    final Class<?> columnClass = getModel().getColumnClass(column);
    final Comparator<?> comparator = super.getComparator(column);
    if (comparator != null && comparator != DefaultSortController.COMPARABLE_COMPARATOR) {
      return comparator;
    }
    if (columnClass == String.class) {
      return Collator.getInstance();
    } else if (Number.class.isAssignableFrom(columnClass)) {
      return new NumericComparator<>();
    } else if (Comparable.class.isAssignableFrom(columnClass)) {
      return COMPARABLE_COMPARATOR;
    } else if (comparator == null) {
      return Collator.getInstance();
    } else {
      return comparator;
    }
  }
}
