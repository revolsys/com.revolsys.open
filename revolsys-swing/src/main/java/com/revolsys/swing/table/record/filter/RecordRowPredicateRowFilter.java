package com.revolsys.swing.table.record.filter;

import java.util.function.Predicate;

import javax.swing.RowFilter;

import com.revolsys.record.Record;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class RecordRowPredicateRowFilter extends RowFilter<RecordRowTableModel, Integer> {
  private final Predicate<? super Record> filter;

  public RecordRowPredicateRowFilter(final Predicate<? super Record> filter) {
    this.filter = filter;
  }

  @Override
  public boolean include(final Entry<? extends RecordRowTableModel, ? extends Integer> entry) {
    final RecordRowTableModel tableModel = entry.getModel();
    final Integer rowIndex = entry.getIdentifier();
    final Record record = tableModel.getRecord(rowIndex);
    return this.filter.test(record);
  }

}
