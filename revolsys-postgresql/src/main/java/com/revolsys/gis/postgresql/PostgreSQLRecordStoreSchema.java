package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.common.collection.map.RefreshableMap;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public class PostgreSQLRecordStoreSchema extends JdbcRecordStoreSchema {

  private final RefreshableMap<PathName, RecordDefinitionImpl> compositeTypes = RefreshableMap
    .supplier(this::refreshCompositeTypes, false);

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStore recordStore) {
    super(recordStore);
  }

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStoreSchema schema,
    final PathName pathName, final String dbName) {
    super(schema, pathName, dbName);
  }

  public PostgreSQLRecordStoreSchema(final PostgreSQLRecordStoreSchema schema,
    final PathName pathName, final String dbName, final boolean quoteName) {
    super(schema, pathName, dbName, quoteName);
  }

  @Override
  public synchronized void refresh() {
    // this.compositeTypes.refresh();
    super.refresh();
  }

  private Map<PathName, RecordDefinitionImpl> refreshCompositeTypes() {
    final Map<PathName, RecordDefinitionImpl> typeByName = new LinkedHashMap<>();
    final PostgreSQLRecordStore recordStore = getRecordStore();
    final String sql = "SELECT udt_schema, udt_name, attribute_name, data_type, is_nullable, coalesce (character_maximum_length,numeric_precision,0) length, coalesce (numeric_scale scale,0), attribute_udt_schema, attribute_udt_name\n"
      + "FROM information_schema.attributes a\n"
      + "order by udt_schema, udt_name, ordinal_position ";
    try (
      Transaction transaction = recordStore.newTransaction(Propagation.REQUIRED);
      JdbcConnection connection = recordStore.getJdbcConnection()) {
      try (
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet resultSet = prepareStatement.executeQuery()) {
        while (resultSet.next()) {
          final String udtSchema = resultSet.getString(1);
          final String udtName = resultSet.getString(2);
          final PathName compositeTypeName = PathName.newPathName("/" + udtSchema + "/" + udtName);
          RecordDefinitionImpl recordDefinition = typeByName.get(compositeTypeName);
          if (recordDefinition == null) {
            recordDefinition = new RecordDefinitionImpl();
            typeByName.put(compositeTypeName, recordDefinition);
          }

          final String dataType = resultSet.getString(3);
          final String fieldName = resultSet.getString(4);
          final boolean required = "NO".equalsIgnoreCase(resultSet.getString(5));
          final int length = resultSet.getInt(6);
          final int scale = resultSet.getInt(7);
          final String attrbuteUdtSchema = resultSet.getString(9);
          final String attrbuteUdtName = resultSet.getString(9);

        }
      } catch (final SQLException e) {
        Logs.error(this, "Error refreshing custom types", e);
      }
    }
    return typeByName;
  }

  @Override
  public synchronized void refreshIfNeeded() {
    // this.compositeTypes.refreshIfNeeded();
    super.refreshIfNeeded();
  }
}
