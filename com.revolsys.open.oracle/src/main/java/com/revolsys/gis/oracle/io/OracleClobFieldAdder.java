package com.revolsys.gis.oracle.io;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.jdbc.attribute.JdbcFieldAdder;

public class OracleClobFieldAdder extends JdbcFieldAdder {

  public OracleClobFieldAdder() {
  }

  @Override
  public FieldDefinition addField(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final OracleJdbcClobFieldDefinition attribute = new OracleJdbcClobFieldDefinition(
      dbName, name, sqlType, length, required, description);
    recordDefinition.addField(attribute);
    return attribute;
  }

}
