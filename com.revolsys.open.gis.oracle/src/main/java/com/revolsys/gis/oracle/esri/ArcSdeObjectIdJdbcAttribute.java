package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class ArcSdeObjectIdJdbcAttribute extends JdbcAttribute {
  public static void replaceAttribute(final String schemaName,
    final DataObjectMetaData metaData, final Integer registrationId,
    final String rowIdColumn) {
    final JdbcAttribute objectIdAttribute = (JdbcAttribute)metaData.getAttribute(rowIdColumn);
    if (objectIdAttribute != null
      && !(objectIdAttribute instanceof ArcSdeObjectIdJdbcAttribute)) {
      final String name = objectIdAttribute.getName();
      final DataType type = objectIdAttribute.getType();
      final int length = objectIdAttribute.getLength();
      final int scale = objectIdAttribute.getScale();
      final boolean required = objectIdAttribute.isRequired();
      final String description = objectIdAttribute.getDescription();
      final Map<String, Object> properties = objectIdAttribute.getProperties();

      final ArcSdeObjectIdJdbcAttribute newObjectIdAttribute = new ArcSdeObjectIdJdbcAttribute(
        name, type, length, scale, required, description, properties,
        schemaName, registrationId);
      final DataObjectMetaDataImpl metaDataImpl = (DataObjectMetaDataImpl)metaData;
      metaDataImpl.replaceAttribute(objectIdAttribute, newObjectIdAttribute);
      if (metaData.getIdAttributeName() == null) {
        metaDataImpl.setIdAttributeName(name);
      }
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcAttribute(final String name, final DataType type,
    final int length, final int scale, final boolean required,
    final String description, final Map<String, Object> properties,
    final String schemaName, final long registrationId) {
    super(name, type, -1, length, scale, required, description, properties);
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
    return new ArcSdeObjectIdJdbcAttribute(getName(), getType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties(),
      this.schemaName, this.registrationId);
  }

  /**
   * Ignore any inserted value.
   */
  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final DataObject object) throws SQLException {
    return parameterIndex;
  }
}
