package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class ArcSdeObjectIdJdbcAttribute extends JdbcAttribute {
  public static void replaceAttribute(final String schemaName,
    final RecordDefinition recordDefinition, final Integer registrationId,
    final String rowIdColumn) {
    final JdbcAttribute objectIdAttribute = (JdbcAttribute)recordDefinition.getAttribute(rowIdColumn);
    if (objectIdAttribute != null
      && !(objectIdAttribute instanceof ArcSdeObjectIdJdbcAttribute)) {
      final String name = objectIdAttribute.getName();
      final String description = objectIdAttribute.getDescription();
      final Map<String, Object> properties = objectIdAttribute.getProperties();

      final ArcSdeObjectIdJdbcAttribute newObjectIdAttribute = new ArcSdeObjectIdJdbcAttribute(
        objectIdAttribute.getDbName(), name, description, properties,
        schemaName, registrationId);
      newObjectIdAttribute.setRecordDefinition(recordDefinition);
      final RecordDefinitionImpl recordDefinitionImpl = (RecordDefinitionImpl)recordDefinition;
      recordDefinitionImpl.replaceAttribute(objectIdAttribute, newObjectIdAttribute);
      if (recordDefinition.getIdAttributeName() == null
        && recordDefinition.getIdAttributeNames().isEmpty()) {
        recordDefinitionImpl.setIdAttributeName(name);
      }
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcAttribute(final String dbName, final String name,
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
  public void addInsertStatementPlaceHolder(final StringBuffer sql,
    final boolean generateKeys) {
    sql.append(" sde.version_user_ddl.next_row_id('");
    sql.append(this.schemaName);
    sql.append("', ");
    sql.append(this.registrationId);
    sql.append(")");
  }

  @Override
  public ArcSdeObjectIdJdbcAttribute clone() {
    return new ArcSdeObjectIdJdbcAttribute(getDbName(), getName(),
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