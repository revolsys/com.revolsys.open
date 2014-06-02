package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.JavaBeanUtil;

public class Between extends Condition {

  private Column column;

  private Value max;

  private Value min;

  public Between(final Column column, final Value min, final Value max) {
    this.column = column;
    this.min = min;
    this.max = max;
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
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
  public void appendDefaultSql(Query query,
    final DataObjectStore dataStore, final StringBuffer buffer) {
    column.appendSql(query, dataStore, buffer);
    buffer.append(" BETWEEN ");
    min.appendSql(query, dataStore, buffer);
    buffer.append(" AND ");
    max.appendSql(query, dataStore, buffer);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    index = column.appendParameters(index, statement);
    index = min.appendParameters(index, statement);
    index = max.appendParameters(index, statement);
    return index;
  }

  @Override
  public Between clone() {
    final Between clone = (Between)super.clone();
    clone.column = JavaBeanUtil.clone(getColumn());
    clone.min = JavaBeanUtil.clone(getMin());
    clone.max = JavaBeanUtil.clone(getMax());
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Between) {
      final Between condition = (Between)obj;
      if (EqualsRegistry.equal(condition.getColumn(), this.getColumn())) {
        if (EqualsRegistry.equal(condition.getMin(), this.getMin())) {
          if (EqualsRegistry.equal(condition.getMax(), this.getMax())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public Column getColumn() {
    return column;
  }

  public Value getMax() {
    return max;
  }

  public Value getMin() {
    return min;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.<QueryValue> asList(column, min, max);
  }

  @Override
  public String toString() {
    return column + " BETWEEN " + min + " AND " + max;
  }
}
