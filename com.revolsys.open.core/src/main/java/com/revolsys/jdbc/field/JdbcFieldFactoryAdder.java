package com.revolsys.jdbc.field;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class JdbcFieldFactoryAdder extends JdbcFieldAdder {
  private final JdbcFieldFactory factory;

  public JdbcFieldFactoryAdder(final JdbcFieldFactory factory) {
    this.factory = factory;
  }

  @Override
  public FieldDefinition addField(final AbstractJdbcRecordStore recordStore,
    final RecordDefinitionImpl recordDefinition, final String dbName, final String name,
    final String dataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final FieldDefinition field = this.factory.newField(dbName, name, dataType, sqlType, length,
      scale, required, description, null);
    recordDefinition.addField(field);
    return field;
  }
}
