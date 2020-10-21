package com.revolsys.record.query;

public interface ConditionComposer {

  ConditionComposer addCondition(Condition condition);

  default ConditionComposer equal(final TableReference table, final CharSequence fieldName,
    final Object value) {
    final ColumnReference field = table.getColumn(fieldName);
    Condition condition;
    if (value == null) {
      condition = new IsNull(field);
    } else {
      QueryValue right;
      if (value instanceof ColumnReference) {
        right = (ColumnReference)value;
      } else if (value instanceof QueryValue) {
        right = (QueryValue)value;
      } else {
        right = new Value(field, value);
      }
      condition = new Equal(field, right);
    }
    return addCondition(condition);
  }

  default ConditionComposer equal(final TableReference fromTable, final String fieldName,
    final TableReference toTable) {
    return equal(fromTable, fieldName, toTable, fieldName);
  }

  default ConditionComposer equal(final TableReference fromTable, final String fromFieldName,
    final TableReference toTable, final String toFieldName) {
    final ColumnReference fromColumn = fromTable.getColumn(fromFieldName);
    final ColumnReference toColumn = toTable.getColumn(toFieldName);
    final Condition condition = new Equal(fromColumn, toColumn);
    return addCondition(condition);
  }

}
