package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class ParenthesisCondition extends AbstractCondition {

  private final Condition condition;

  public ParenthesisCondition(final Condition condition) {
    this.condition = condition;
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return condition.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append("(");
    condition.appendSql(buffer);
    buffer.append(")");
  }

  @Override
  public ParenthesisCondition clone() {
    return new ParenthesisCondition(condition.clone());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ParenthesisCondition) {
      final ParenthesisCondition condition = (ParenthesisCondition)obj;
      if (EqualsRegistry.equal(condition.getCondition(), this.getCondition())) {
        return true;
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

  @Override
  public String toString() {
    return "(" + getCondition() + ")";
  }
}
