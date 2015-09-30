package com.revolsys.swing.table.record.row;

import java.util.function.Function;

import com.revolsys.record.Record;
import com.revolsys.swing.action.enablecheck.AbstractValueEnableCheck;

public class RecordRowFunctionEnableCheck<R extends Record, V> extends AbstractValueEnableCheck {
  public static <R1 extends Record, V1> RecordRowFunctionEnableCheck<R1, V1> newEnableCheck(
    final boolean invert, final Object expectedValue, final Function<R1, V1> valueFunction) {
    return new RecordRowFunctionEnableCheck<>(invert, expectedValue, valueFunction);
  }

  public static <R1 extends Record, V1> RecordRowFunctionEnableCheck<R1, V1> newEnableCheck(
    final Object expectedValue, final Function<R1, V1> valueFunction) {
    return new RecordRowFunctionEnableCheck<>(expectedValue, valueFunction);
  }

  private Function<R, V> valueFunction;

  public RecordRowFunctionEnableCheck(final boolean invert, final Object expectedValue,
    final Function<R, V> valueFunction) {
    super(invert, expectedValue);
    this.valueFunction = valueFunction;
  }

  public RecordRowFunctionEnableCheck(final Object expectedValue,
    final Function<R, V> valueFunction) {
    this(false, expectedValue, valueFunction);
  }

  @Override
  public Object getValue() {
    final R record = RecordRowTable.getEventRecord();
    if (record == null) {
      return null;
    } else {
      final V value = this.valueFunction.apply(record);
      return value;
    }
  }

  @Override
  public String toString() {
    return this.valueFunction + " " + super.toString();
  }
}
