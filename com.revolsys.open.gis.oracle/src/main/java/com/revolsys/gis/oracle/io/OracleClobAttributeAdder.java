package com.revolsys.gis.oracle.io;

import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;

public class OracleClobAttributeAdder extends JdbcAttributeAdder {

  public OracleClobAttributeAdder() {
  }

  @Override
  public Attribute addAttribute(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final OracleJdbcClobAttribute attribute = new OracleJdbcClobAttribute(
      dbName, name, sqlType, length, required, description);
    recordDefinition.addAttribute(attribute);
    return attribute;
  }

}
