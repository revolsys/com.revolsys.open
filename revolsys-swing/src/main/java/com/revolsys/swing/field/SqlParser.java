package com.revolsys.swing.field;

import com.revolsys.record.query.Condition;

public interface SqlParser {

  String getSqlPrefix();

  Condition whereToCondition(String where);
}
