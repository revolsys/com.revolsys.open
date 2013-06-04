package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  public List<Condition> getConditions() {
    return conditions;

  }

  public boolean isEmpty() {
    return conditions.isEmpty();
  }
}
