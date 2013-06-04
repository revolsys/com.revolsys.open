package com.revolsys.gis.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.util.CollectionUtil;

public class MultipleCondition extends AbstractMultiCondition {

  private final String operator;

  public MultipleCondition(final String operator,
    final Collection<? extends Condition> conditions) {
    super(conditions);
    this.operator = operator;
  }

  public MultipleCondition(final String operator, final Condition... conditions) {
    this(operator, Arrays.asList(conditions));
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append("(");
    boolean first = true;

    for (final Condition condition : getConditions()) {
      if (first) {
        first = false;
      } else {
        buffer.append(" ");
        buffer.append(operator);
        buffer.append(" ");
      }
      condition.appendSql(buffer);
    }
    buffer.append(")");
  }

  @Override
  public MultipleCondition clone() {
    final List<Condition> conditions = cloneConditions();
    return new MultipleCondition(operator, conditions);
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public String toString() {
    return "(" + CollectionUtil.toString(" " + operator + " ", getConditions())
      + ")";
  }
}
