package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import com.revolsys.collection.map.MapEx;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.field.JdbcFieldDefinitions;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public interface JdbcRecordStore extends RecordStore {

  default void execteBatch(final PreparedStatement statement) throws SQLException {
    statement.executeBatch();
  }

  default int executeUpdate(final String sql, final Object... parameters) {
    try (
      Transaction transaction = newTransaction(Propagation.REQUIRED);
      final JdbcConnection connection = getJdbcConnection()) {
      try {
        return JdbcUtils.executeUpdate(connection, sql, parameters);
      } catch (final SQLException e) {
        throw connection.getException("Update", sql, e);
      }
    }
  }

  String getGeneratePrimaryKeySql(JdbcRecordDefinition recordDefinition);

  JdbcConnection getJdbcConnection();

  JdbcConnection getJdbcConnection(boolean autoCommit);

  JdbcRecordDefinition getRecordDefinition(String tableName, ResultSetMetaData resultSetMetaData,
    String dbTableName);

  default ResultSet getResultSet(final PreparedStatement statement, final Query query)
    throws SQLException {
    setPreparedStatementParameters(statement, query);
    return statement.executeQuery();
  }

  PreparedStatement insertStatementPrepareRowId(JdbcConnection connection,
    RecordDefinition recordDefinition, String sql) throws SQLException;

  boolean isIdFieldRowid(RecordDefinition recordDefinition);

  default void lockTable(final String typePath) {
    try (
      final JdbcConnection connection = getJdbcConnection()) {
      final String tableName = JdbcUtils.getQualifiedTableName(typePath);
      final String sql = "LOCK TABLE " + tableName + " IN SHARE MODE";
      JdbcUtils.executeUpdate(connection, sql);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to lock table " + typePath, e);
    }

  }

  default int selectInt(final String sql, final Object... parameters) {
    try (
      JdbcConnection connection = getJdbcConnection()) {
      try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
        JdbcUtils.setParameters(statement, parameters);

        try (
          final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return resultSet.getInt(1);
          } else {
            throw new IllegalArgumentException("Value not found");
          }
        }
      } catch (final SQLException e) {
        throw connection.getException("selectInt", sql, e);
      }
    }
  }

  default long selectLong(final String sql, final Object... parameters) {
    try (
      JdbcConnection connection = getJdbcConnection()) {
      try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
        JdbcUtils.setParameters(statement, parameters);

        try (
          final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return resultSet.getLong(1);
          } else {
            throw new IllegalArgumentException("Value not found");
          }
        }
      } catch (final SQLException e) {
        throw connection.getException("selectInt", sql, e);
      }
    }
  }

  default MapEx selectMap(final String sql, final Object... parameters) {
    try (
      JdbcConnection connection = getJdbcConnection()) {
      try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
        JdbcUtils.setParameters(statement, parameters);

        try (
          final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return JdbcUtils.readMap(resultSet);
          } else {
            throw new IllegalArgumentException(
              "Value not found for " + sql + " " + Arrays.asList(parameters));
          }
        }
      } catch (final SQLException e) {
        throw connection.getException(null, sql, e);
      }
    }
  }

  default String selectString(final String sql, final Object... parameters) throws SQLException {
    try (
      JdbcConnection connection = getJdbcConnection()) {
      return JdbcUtils.selectString(connection, sql, parameters);
    }
  }

  default void setPreparedStatementParameters(final PreparedStatement statement,
    final Query query) {
    int index = 1;
    for (final Object parameter : query.getParameters()) {
      final JdbcFieldDefinition field = JdbcFieldDefinitions.newFieldDefinition(parameter);
      try {
        index = field.setPreparedStatementValue(statement, index, parameter);
      } catch (final SQLException e) {
        throw new RuntimeException("Error setting value:" + parameter, e);
      }
    }
    final Condition where = query.getWhereCondition();
    if (!where.isEmpty()) {
      where.appendParameters(index, statement);
    }
  }

}
