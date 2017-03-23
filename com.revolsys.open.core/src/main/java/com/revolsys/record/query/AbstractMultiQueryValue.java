package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.revolsys.datatype.DataType;
import com.revolsys.util.Exceptions;

public abstract class AbstractMultiQueryValue implements QueryValue {

  private List<QueryValue> values = new ArrayList<>();

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
      return this.values.add(value);
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : getQueryValues()) {
      if (value != null) {
        index = value.appendParameters(index, statement);
      }
    }
    return index;
  }

  public void clear() {
    this.values.clear();
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
      final List<QueryValue> values1 = getQueryValues();
      final List<QueryValue> values2 = value.getQueryValues();
      if (values1.size() == values2.size()) {
        for (int i = 0; i < values1.size(); i++) {
          final QueryValue value1 = values1.get(i);
          final QueryValue value2 = values2.get(i);
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
    return Collections.<QueryValue> unmodifiableList(this.values);
  }

  public boolean isEmpty() {
    return this.values.isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(
    final Function<QueryValue, QueryValue> valueHandler) {
    List<QueryValue> newValues = null;

    final int index = 0;
    for (final QueryValue queryValue : this.values) {
      final QueryValue newValue = valueHandler.apply(queryValue);
      if (queryValue != newValue) {
        if (newValues == null) {
          newValues = new ArrayList<>(this.values);
        }
      }
      if (newValues != null) {
        newValues.set(index, newValue);
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
