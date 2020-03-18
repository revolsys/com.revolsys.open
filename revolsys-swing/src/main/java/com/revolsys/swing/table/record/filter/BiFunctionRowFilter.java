package com.revolsys.swing.table.record.filter;

import java.util.function.BiFunction;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

public class BiFunctionRowFilter<M extends TableModel> extends RowFilter<M, Integer> {

  public static <M1 extends TableModel> BiFunctionRowFilter<M1> newFilter(
    final BiFunction<M1, Integer, Boolean> function) {
    if (function == null) {
      return null;
    } else {
      return new BiFunctionRowFilter<>(function);
    }
  }

  private final BiFunction<M, Integer, Boolean> function;

  public BiFunctionRowFilter(final BiFunction<M, Integer, Boolean> function) {
    this.function = function;
  }

  @Override
  public boolean include(final Entry<? extends M, ? extends Integer> entry) {
    final M model = entry.getModel();
    final Integer identifier = entry.getIdentifier();
    return this.function.apply(model, identifier);
  }

}
