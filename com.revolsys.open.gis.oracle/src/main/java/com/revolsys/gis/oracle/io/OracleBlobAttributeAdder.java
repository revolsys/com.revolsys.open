package com.revolsys.gis.oracle.io;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.jdbc.attribute.JdbcFieldAdder;

public class OracleBlobAttributeAdder extends JdbcFieldAdder {

  public OracleBlobAttributeAdder() {
  }

  @Override
  public FieldDefinition addField(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final OracleJdbcBlobFieldDefinition attribute = new OracleJdbcBlobFieldDefinition(
      dbName, name, sqlType, length, required, description);
    recordDefinition.addField(attribute);
    return attribute;
  }

}
