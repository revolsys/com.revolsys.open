package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public abstract class AbstractMultiCondition extends AbstractCondition {

  private final List<Condition> conditions;

  public AbstractMultiCondition(final Collection<? extends Condition> conditions) {
    this.conditions = new ArrayList<Condition>(conditions);
  }

  public void add(final Condition condition) {
    conditions.add(condition);
  }

  public void add(final String sql) {
    final SqlCondition condition = new SqlCondition(sql);
    add(condition);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final Condition condition : conditions) {
      index = condition.appendParameters(index, statement);
    }
    return index;
  }

  public void clear() {
    conditions.clear();
  }

  public List<Condition> cloneConditions() {
    final List<Condition> conditions = new ArrayList<Condition>();
    for (final Condition condition : this.conditions) {
      conditions.add(condition.clone());
    }
    return conditions;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMultiCondition) {
      final AbstractMultiCondition condition = (AbstractMultiCondition)obj;
      final List<Condition> conditions1 = getConditions();
      final List<Condition> conditions2 = condition.getConditions();
      if (conditions1.size() == conditions2.size()) {
        for (int i = 0; i < conditions1.size(); i++) {
          final Condition condition1 = conditions1.get(i);
          final Condition condition2 = conditions2.get(i);
          if (!EqualsRegistry.equal(condition1, condition2)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Condition> getConditions() {
    return conditions;

  }

  @Override
  public boolean isEmpty() {
    return conditions.isEmpty();
  }
}
