package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

public abstract class AbstractMultiQueryValue implements QueryValue {
  private static final QueryValue[] EMPTY_ARRAY = new QueryValue[0];

  protected QueryValue[] values = EMPTY_ARRAY;

  public AbstractMultiQueryValue() {
  }

  public AbstractMultiQueryValue(final Iterable<? extends QueryValue> values) {
    if (values != null) {
      for (final QueryValue value : values) {
        addValue(value);
      }
    }
  }

  protected boolean addValue(final QueryValue value) {
    if (value == null) {
      return false;
    } else {
      final QueryValue[] oldValues = this.values;
      final QueryValue[] values = new QueryValue[oldValues.length + 1];
      System.arraycopy(oldValues, 0, values, 0, oldValues.length);
      values[oldValues.length] = value;
      this.values = values;
      return true;
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : this.values) {
      if (value != null) {
        index = value.appendParameters(index, statement);
      }
    }
    return index;
  }

  public void clear() {
    this.values = EMPTY_ARRAY;
  }

  @Override
  public AbstractMultiQueryValue clone() {
    try {
      final AbstractMultiQueryValue clone = (AbstractMultiQueryValue)super.clone();
      clone.values = QueryValue.cloneQueryValues(this.values);
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMultiQueryValue) {
      final AbstractMultiQueryValue value = (AbstractMultiQueryValue)obj;
      final QueryValue[] values1 = this.values;
      final QueryValue[] values2 = value.values;
      if (values1.length == values2.length) {
        for (int i = 0; i < values1.length; i++) {
          final QueryValue value1 = values1[i];
          final QueryValue value2 = values2[i];
          if (!DataType.equal(value1, value2)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.values);
  }

  public boolean isEmpty() {
    return this.values.length == 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(
    final Function<QueryValue, QueryValue> valueHandler) {
    QueryValue[] newValues = null;

    final int index = 0;
    for (final QueryValue queryValue : this.values) {
      final QueryValue newValue = valueHandler.apply(queryValue);
      if (queryValue != newValue) {
        if (newValues == null) {
          newValues = QueryValue.cloneQueryValues(this.values);
        }
      }
      if (newValues != null) {
        newValues[index] = newValue;
      }
    }
    if (newValues == null) {
      return (QV)this;
    } else {
      final AbstractMultiQueryValue clone = clone();
      clone.values = newValues;
      return (QV)clone;
    }
  }
}
