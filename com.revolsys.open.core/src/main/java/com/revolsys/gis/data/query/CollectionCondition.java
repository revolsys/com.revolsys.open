package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.CollectionUtil;

public class CollectionCondition extends AbstractCondition {
  private final List<Condition> conditions = new ArrayList<Condition>();

  public CollectionCondition(final Collection<? extends Condition> conditions) {
    this.conditions.addAll(conditions);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final Condition condition : conditions) {
      index = condition.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append('(');
    for (int i = 0; i < conditions.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      final Condition condition = conditions.get(i);
      condition.appendSql(buffer);
    }
    buffer.append(')');
  }

  @Override
  public CollectionCondition clone() {
    return new CollectionCondition(conditions);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CollectionCondition) {
      final CollectionCondition condition = (CollectionCondition)obj;
      return EqualsRegistry.equal(condition.getConditions(),
        this.getConditions());
    } else {
      return false;
    }
  }

  @Override
  public List<Condition> getConditions() {
    return conditions;
  }

  @Override
  public String toString() {
    return "(" + CollectionUtil.toString(conditions) + ")";
  }
}
