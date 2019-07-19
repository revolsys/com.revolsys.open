package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.JavaBeanUtil;

public class Between extends AbstractUnaryQueryValue implements Condition {

  private Value max;

  private Value min;

  public Between(final Column column, final Value min, final Value max) {
    super(column);
    this.min = min;
    this.max = max;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    super.appendDefaultSql(query, recordStore, buffer);
    buffer.append(" BETWEEN ");
    this.min.appendSql(query, recordStore, buffer);
    buffer.append(" AND ");
    this.max.appendSql(query, recordStore, buffer);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    index = super.appendParameters(index, statement);
    index = this.min.appendParameters(index, statement);
    index = this.max.appendParameters(index, statement);
    return index;
  }

  @Override
  public Between clone() {
    final Between clone = (Between)super.clone();
    clone.min = JavaBeanUtil.clone(getMin());
    clone.max = JavaBeanUtil.clone(getMax());
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Between) {
      final Between condition = (Between)obj;
      if (DataType.equal(condition.getMin(), this.getMin())) {
        if (DataType.equal(condition.getMax(), this.getMax())) {
          return super.equals(condition);
        }
      }
    }
    return false;
  }

  public Column getColumn() {
    return super.getValue();
  }

  public Value getMax() {
    return this.max;
  }

  public Value getMin() {
    return this.min;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.<QueryValue> asList(getValue(), this.min, this.max);
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue colum = getColumn();
    final Object columnValue = colum.getValue(record);
    if (columnValue == null) {
      return false;
    } else {
      final QueryValue min = getMin();
      final Object minValue = min.getValue(record);
      if (minValue == null || CompareUtil.compare(minValue, columnValue) > 0) {
        return false;
      } else {
        final QueryValue max = getMax();
        final Object maxValue = max.getValue(record);
        if (maxValue == null || CompareUtil.compare(maxValue, columnValue) < 0) {
          return false;
        } else {
          return true;
        }
      }
    }
  }

  @Override
  public String toString() {
    return getColumn() + " BETWEEN " + this.min + " AND " + this.max;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(
    final Function<QueryValue, QueryValue> valueHandler) {
    Between between = super.updateQueryValues(valueHandler);
    final Value min = (Value)valueHandler.apply(this.min);
    final Value max = (Value)valueHandler.apply(this.max);
    if (between == this) {
      if (min == this.min && max == this.max) {
        return (QV)this;
      } else {
        between = clone();
      }
    }
    between.min = min;
    between.max = max;
    return (QV)between;
  }
}
