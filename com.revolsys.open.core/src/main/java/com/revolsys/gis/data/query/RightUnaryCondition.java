package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class RightUnaryCondition extends AbstractCondition {

  private final Condition condition;

  private final String operator;

  public RightUnaryCondition(final Condition condition, final String operator) {
    this.operator = operator;
    this.condition = condition;
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return condition.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    condition.appendSql(buffer);
    buffer.append(" ");
    buffer.append(operator);
  }

  @Override
  public RightUnaryCondition clone() {
    return new RightUnaryCondition(condition.clone(), operator);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof RightUnaryCondition) {
      final RightUnaryCondition condition = (RightUnaryCondition)obj;
      if (EqualsRegistry.equal(condition.getCondition(), this.getCondition())) {
        if (EqualsRegistry.equal(condition.getOperator(), this.getOperator())) {
          return true;
        }
      }
    }
    return false;
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
    return getCondition() + " " + getOperator();
  }
}
