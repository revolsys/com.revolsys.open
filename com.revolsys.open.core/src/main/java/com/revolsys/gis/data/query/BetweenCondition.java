package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.JavaBeanUtil;

public class BetweenCondition extends AbstractCondition {

  private final Value min;

  private final Value max;

  private final Column column;

  public BetweenCondition(final Column column, final Value min, final Value max) {
    this.column = column;
    this.min = min;
    this.max = max;
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    index = column.appendParameters(index, statement);
    index = min.appendParameters(index, statement);
    index = max.appendParameters(index, statement);
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    column.appendSql(buffer);
    buffer.append(" BETWEEN ");
    min.appendSql(buffer);
    buffer.append(" AND ");
    max.appendSql(buffer);
  }

  @Override
  public BetweenCondition clone() {
    final Column column = JavaBeanUtil.clone(getColumn());
    final Value min = JavaBeanUtil.clone(getMin());
    final Value max = JavaBeanUtil.clone(getMax());
    return new BetweenCondition(column, min, max);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BetweenCondition) {
      final BetweenCondition condition = (BetweenCondition)obj;
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

  @Override
  public List<Condition> getConditions() {
    return Arrays.<Condition> asList(column, min, max);
  }

  public Value getMax() {
    return max;
  }

  public Value getMin() {
    return min;
  }

  @Override
  public String toString() {
    return column + " BETWEEN " + min + " AND " + max;
  }
}
