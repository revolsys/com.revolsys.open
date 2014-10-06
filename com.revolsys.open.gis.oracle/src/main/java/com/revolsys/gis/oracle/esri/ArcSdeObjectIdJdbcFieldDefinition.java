package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.attribute.JdbcFieldDefinition;

public class ArcSdeObjectIdJdbcFieldDefinition extends JdbcFieldDefinition {
  public static void replaceAttribute(final String schemaName,
    final RecordDefinition recordDefinition, final Integer registrationId,
    final String rowIdColumn) {
    final JdbcFieldDefinition objectIdAttribute = (JdbcFieldDefinition)recordDefinition.getField(rowIdColumn);
    if (objectIdAttribute != null
      && !(objectIdAttribute instanceof ArcSdeObjectIdJdbcFieldDefinition)) {
      final String name = objectIdAttribute.getName();
      final String description = objectIdAttribute.getDescription();
      final Map<String, Object> properties = objectIdAttribute.getProperties();

      final ArcSdeObjectIdJdbcFieldDefinition newObjectIdAttribute = new ArcSdeObjectIdJdbcFieldDefinition(
        objectIdAttribute.getDbName(), name, description, properties,
        schemaName, registrationId);
      newObjectIdAttribute.setRecordDefinition(recordDefinition);
      final RecordDefinitionImpl recordDefinitionImpl = (RecordDefinitionImpl)recordDefinition;
      recordDefinitionImpl.replaceField(objectIdAttribute, newObjectIdAttribute);
      if (recordDefinition.getIdFieldName() == null
        && recordDefinition.getIdFieldNames().isEmpty()) {
        recordDefinitionImpl.setIdFieldName(name);
      }
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcFieldDefinition(final String dbName, final String name,
    final String description, final Map<String, Object> properties,
    final String schemaName, final long registrationId) {
    super(dbName, name, DataTypes.INT, -1, 19, 0, true, description, properties);
    this.schemaName = schemaName;
    this.registrationId = registrationId;
  }

  /**
   * Generate an OBJECT ID using ESRI's sde.version_user_ddl.next_row_id
   * function.
   */
  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql,
    final boolean generateKeys) {
    sql.append(" sde.version_user_ddl.next_row_id('");
    sql.append(this.schemaName);
    sql.append("', ");
    sql.append(this.registrationId);
    sql.append(")");
  }

  @Override
  public ArcSdeObjectIdJdbcFieldDefinition clone() {
    return new ArcSdeObjectIdJdbcFieldDefinition(getDbName(), getName(),
      getDescription(), getProperties(), this.schemaName, this.registrationId);
  }

  /**
   * Ignore any inserted value.
   */
  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    return parameterIndex;
  }
}
