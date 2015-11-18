package com.revolsys.swing.table.record.filter;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import com.revolsys.util.function.Function2;

public class Function2RowFilter<M extends TableModel> extends RowFilter<M, Integer> {

  public static <M1 extends TableModel> Function2RowFilter<M1> newFilter(
    final Function2<M1, Integer, Boolean> function) {
    if (function == null) {
      return null;
    } else {
      return new Function2RowFilter<>(function);
    }
  }

  private final Function2<M, Integer, Boolean> function;

  public Function2RowFilter(final Function2<M, Integer, Boolean> function) {
    this.function = function;
  }

  @Override
  public boolean include(final Entry<? extends M, ? extends Integer> entry) {
    final M model = entry.getModel();
    final Integer identifier = entry.getIdentifier();
    return this.function.apply(model, identifier);
  }

}
