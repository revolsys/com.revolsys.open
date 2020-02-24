package com.revolsys.geopackage.old;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.sqlite.SQLiteConnection;
import org.sqlite.core.DB;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStoreSchemaElement;

public class GeoPackageRecordStore extends AbstractJdbcRecordStore {
  public GeoPackageRecordStore(final DataSource dataSource) {
    super(dataSource);
  }

  public GeoPackageRecordStore(final GeoPackage geoPackage,
    final Map<String, ? extends Object> connectionProperties) {
    super(geoPackage, connectionProperties);
  }

  @Override
  protected Set<String> getDatabaseSchemaNames() {
    return Collections.emptySet();
  }

  @Override
  public Identifier getNextPrimaryKey(final String typePath) {
    return null;
  }

  @Override
  public String getRecordStoreType() {
    return "GeoPackageFactory";
  }

  @Override
  public String getSequenceName(final JdbcRecordDefinition recordDefinition) {
    return null;
  }

  @Override
  @PostConstruct
  public void initializeDo() {
    super.initializeDo();
    setUsesSchema(false);

    setExcludeTablePatterns("/TRIGGER_.*_FEATURE_COUNT.*");
    setExcludeTablePaths("/OGR_EMPTY_TABLE");
    final String filter = "WHERE NOT (NAME LIKE 'GPKG%' OR NAME LIKE 'RTREE%' OR NAME LIKE 'SQLITE%')";
    setSchemaTablePermissionsSql(
      "select  '/' \"SCHEMA_NAME\", name \"TABLE_NAME\", 'ALL' \"PRIVILEGE\", '' \"REMARKS\"  from sqlite_master "
        + filter + " union all "
        + "select  '/' \"SCHEMA_NAME\", name \"TABLE_NAME\", 'ALL' \"PRIVILEGE\", '' \"REMARKS\"  from sqlite_temp_master "
        + filter);

    addFieldAdder("BOOLEAN", DataTypes.BOOLEAN);
    addFieldAdder("TINYINT", DataTypes.BYTE);
    addFieldAdder("SMALLINT", DataTypes.SHORT);
    addFieldAdder("MEDIUMINT", DataTypes.INT);
    addFieldAdder("INT", DataTypes.LONG);
    addFieldAdder("INTEGER", DataTypes.LONG);
    addFieldAdder("FLOAT", DataTypes.FLOAT);
    addFieldAdder("DOUBLE", DataTypes.DOUBLE);
    addFieldAdder("REAL", DataTypes.DOUBLE);
    addFieldAdder("TEXT", DataTypes.STRING);
    addFieldAdder("BLOB", DataTypes.BLOB);
    addFieldAdder("DATE", DataTypes.SQL_DATE);
    addFieldAdder("DATETIME", DataTypes.DATE_TIME);

    final GeoPackageGeometryFieldAdder geometryAdder = new GeoPackageGeometryFieldAdder();
    addFieldAdder("GEOMETRY", geometryAdder);
    addFieldAdder("POINT", geometryAdder);
    addFieldAdder("LINESTRING", geometryAdder);
    addFieldAdder("POLYGON", geometryAdder);
    addFieldAdder("GEOMETRYCOLLECTION", geometryAdder);
    addFieldAdder("MULTIPOINT", geometryAdder);
    addFieldAdder("MULTILINESTRING", geometryAdder);
    addFieldAdder("MULTIPOLYGON", geometryAdder);

    super.initialize();
    try (
      JdbcConnection connection = getJdbcConnection(true)) {
      final SQLiteConnection sqliteConnection = (SQLiteConnection)((DelegatingConnection<?>)connection
        .getConnection()).getInnermostDelegate();
      final DB db = sqliteConnection.getDatabase();
      db.enable_load_extension(true);
      try {
        // db._exec("select load_extension('libgpkg')");
      } finally {
        db.enable_load_extension(false);
      }
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public PreparedStatement insertStatementPrepareRowId(final JdbcConnection connection,
    final RecordDefinition recordDefinition, final String sql) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return false;
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElementsDo(
    final JdbcRecordStoreSchema schema, final PathName schemaPath) {
    final String schemaName = schema.getPath();
    final Map<String, String> tableDescriptionMap = new HashMap<>();
    final Map<String, List<String>> tablePermissionsMap = new TreeMap<>();
    final Map<PathName, JdbcRecordDefinition> recordDefinitionMap = loadRecordDefinitionsPermissions(
      schema);

    final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    try {
      try (
        final Connection connection = getJdbcConnection()) {
        for (final JdbcRecordDefinition recordDefinition : recordDefinitionMap.values()) {
          final PathName typePath = recordDefinition.getPathName();
          elementsByPath.put(typePath, recordDefinition);
        }
        for (final JdbcRecordDefinition recordDefinition : recordDefinitionMap.values()) {
          final String tableName = recordDefinition.getDbTableName();
          final List<String> idFieldNames = new ArrayList<>();
          try (
            PreparedStatement columnStatement = connection
              .prepareStatement("PRAGMA table_info(" + tableName + ")")) {
            try (
              final ResultSet columnsRs = columnStatement.executeQuery()) {
              while (columnsRs.next()) {
                final String dbColumnName = columnsRs.getString("name");
                final String fieldName = dbColumnName.toUpperCase();
                final int sqlType = Types.OTHER;
                String dataType = columnsRs.getString("type");
                int length = -1;
                final int scale = -1;
                if (dataType.startsWith("TEXT(")) {
                  length = Integer.parseInt(dataType.substring(5, dataType.length() - 1));
                  dataType = "TEXT";
                }
                final boolean required = columnsRs.getString("notnull").equals("1");
                final boolean primaryKey = columnsRs.getString("pk").equals("1");
                if (primaryKey) {
                  idFieldNames.add(fieldName);
                }
                final Object defaultValue = columnsRs.getString("dflt_value");
                final FieldDefinition field = addField(recordDefinition, dbColumnName, fieldName,
                  dataType, sqlType, length, scale, required, null);
                field.setDefaultValue(defaultValue);
              }
            }
          }
          recordDefinition.setIdFieldNames(idFieldNames);
        }
      }
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to load metadata for schema " + schemaName, e);
    }

    return elementsByPath;
  }

}
