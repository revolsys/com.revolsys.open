package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

public class LeftUnaryCondition extends AbstractCondition {

  private final Condition condition;

  private final String operator;

  public LeftUnaryCondition(final String operator, final Condition condition) {
    this.operator = operator;
    this.condition = condition;
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return condition.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append(operator);
    buffer.append(" ");
    condition.appendSql(buffer);
  }

  @Override
  public LeftUnaryCondition clone() {
    return new LeftUnaryCondition(operator, condition.clone());
  }

  public Condition getCondition() {
    return condition;
  }

  @Override
  public List<Condition> getConditions() {
    return Collections.singletonList(condition);
  }

  public String getOperator() {
    return operator;
  }

  @Override
  public String toString() {
    return operator + " " + condition;
  }
}
