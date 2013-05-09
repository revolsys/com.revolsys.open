package com.revolsys.gis.data.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.util.CollectionUtil;

public class MultipleCondition implements Condition {

  private final String operator;

  private final List<Condition> conditions;

  public MultipleCondition(final String operator, Condition... conditions) {
    this(operator, Arrays.asList(conditions));
  }

  public MultipleCondition(String operator,
    Collection<? extends Condition> conditions) {
    this.operator = operator;
    this.conditions = new ArrayList<Condition>(conditions);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append("(");
    boolean first = true;

    for (Condition condition : conditions) {
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
  public String toString() {
    return "(" + CollectionUtil.toString(" " + operator + " ", conditions)
      + ")";
  }
}
