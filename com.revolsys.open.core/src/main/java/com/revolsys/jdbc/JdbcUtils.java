package com.revolsys.jdbc;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Path;
import com.revolsys.jdbc.attribute.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.util.Property;

public final class JdbcUtils {
  public static void addColumnNames(final StringBuilder sql,
    final RecordDefinition recordDefinition, final String tablePrefix) {
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final FieldDefinition attribute = recordDefinition.getField(i);
      addFieldName(sql, tablePrefix, attribute);
    }
  }

  public static void addColumnNames(final StringBuilder sql,
    final RecordDefinition recordDefinition, final String tablePrefix,
    final List<String> fieldNames, boolean hasColumns) {
    for (final String fieldName : fieldNames) {
      if (hasColumns) {
        sql.append(", ");
      }
      final FieldDefinition attribute = recordDefinition.getField(fieldName);
      if (attribute == null) {
        sql.append(fieldName);
      } else {
        addFieldName(sql, tablePrefix, attribute);
      }
      hasColumns = true;
    }
  }

  public static void addFieldName(final StringBuilder sql,
    final String tablePrefix, final FieldDefinition attribute) {
    if (attribute instanceof JdbcFieldDefinition) {
      final JdbcFieldDefinition jdbcAttribute = (JdbcFieldDefinition)attribute;
      jdbcAttribute.addColumnName(sql, tablePrefix);
    } else {
      sql.append(attribute.getName());
    }
  }

  public static void addOrderBy(final StringBuilder sql,
    final Map<String, Boolean> orderBy) {
    if (!orderBy.isEmpty()) {
      sql.append(" ORDER BY ");
      for (final Iterator<Entry<String, Boolean>> iterator = orderBy.entrySet()
          .iterator(); iterator.hasNext();) {
        final Entry<String, Boolean> entry = iterator.next();
        final String column = entry.getKey();
        sql.append(column);
        final Boolean ascending = entry.getValue();
        if (!ascending) {
          sql.append(" DESC");
        }
        if (iterator.hasNext()) {
          sql.append(", ");
        }
      }
    }
  }

  public static void appendWhere(final StringBuilder sql, final Query query) {
    final Condition where = query.getWhereCondition();
    if (where != null && !where.isEmpty()) {
      sql.append(" WHERE ");
      final RecordDefinition recordDefinition = query.getRecordDefinition();
      if (recordDefinition == null) {
        where.appendSql(query, null, sql);
      } else {
        final RecordStore recordStore = recordDefinition.getRecordStore();
        where.appendSql(query, recordStore, sql);
      }
    }
  }

  public static String cleanObjectName(final String objectName) {
    return objectName.replaceAll("[^a-zA-Z\\._]", "");
  }

  public static void close(final ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (final SQLException e) {
        LOG.debug("SQL error closing result set", e);
      } catch (final Throwable e) {
        LOG.debug("Unknown error closing result set", e);
      }
    }
  }

  public static void close(final Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (final SQLException e) {
        LOG.debug("SQL error closing statement", e);
      } catch (final Throwable e) {
        LOG.debug("Unknown error closing statement", e);
      }
    }
  }

  public static void close(final Statement statement, final ResultSet resultSet) {
    close(resultSet);
    close(statement);
  }

  public static void commit(final Connection connection) {
    try {
      connection.commit();
    } catch (final SQLException e) {
    }
  }

  public static String createSelectSql(final RecordDefinition recordDefinition,
    final String tablePrefix, final String fromClause,
    final boolean lockResults, final List<String> fieldNames,
    final Query query, final Map<String, Boolean> orderBy) {
    final String typePath = recordDefinition.getPath();
    final StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");
    boolean hasColumns = false;
    if (fieldNames.isEmpty() || fieldNames.remove("*")) {
      addColumnNames(sql, recordDefinition, tablePrefix);
      hasColumns = true;
    }
    addColumnNames(sql, recordDefinition, tablePrefix, fieldNames, hasColumns);
    sql.append(" FROM ");
    if (Property.hasValue(fromClause)) {
      sql.append(fromClause);
    } else {
      final String tableName = getQualifiedTableName(typePath);
      sql.append(tableName);
      sql.append(" ");
      sql.append(tablePrefix);
    }
    appendWhere(sql, query);
    addOrderBy(sql, orderBy);
    if (lockResults) {
      sql.append(" FOR UPDATE");
    }
    return sql.toString();
  }

  public static void delete(final Connection connection,
    final String tableName, final String idColumn, final Object id) {

    final String sql = "DELETE FROM " + cleanObjectName(tableName) + " WHERE "
        + cleanObjectName(idColumn) + " = ?";
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        setValue(statement, 1, id);
        statement.executeQuery();
      } catch (final SQLException e) {
        LOG.error("Unable to delete:" + sql, e);
        throw new RuntimeException("Unable to delete:" + sql, e);
      } finally {
        close(statement);
        connection.commit();
      }
    } catch (final SQLException e) {
      LOG.error("Invalid table name or id column: " + sql, e);
      throw new IllegalArgumentException("Invalid table name or id column: "
          + sql);
    }

  }

  public static int executeUpdate(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      return statement.executeUpdate();
    } finally {
      close(statement);
    }
  }

  public static int executeUpdate(final DataSource dataSource,
    final String sql, final Object... parameters) {
    final Connection connection = getConnection(dataSource);
    try {
      return executeUpdate(connection, sql, parameters);
    } catch (final SQLException e) {
      throw getException(dataSource, connection, "Update", sql, e);
    } finally {
      release(connection, dataSource);
    }
  }

  public static int executeUpdate(final JdbcRecordStore recordStore,
    final String sql, final Object... parameters) {
    try (
        final JdbcConnection connection = recordStore.getJdbcConnection()) {
      try {
        return executeUpdate(connection, sql, parameters);
      } catch (final SQLException e) {
        throw connection.getException("Update", sql, e);
      }
    }
  }

  public static Connection getConnection(final DataSource dataSource) {
    return DataSourceUtils.getConnection(dataSource);
  }

  public static String getDeleteSql(final Query query) {
    final String tableName = query.getTypeName();
    final String dbTableName = getQualifiedTableName(tableName);

    final StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(dbTableName);
    sql.append(" T ");
    appendWhere(sql, query);
    return sql.toString();
  }

  public static DataAccessException getException(final DataSource dataSource,
    final Connection connection, final String task, final String sql,
    final SQLException e) {
    if (dataSource == null) {
      return new SQLStateSQLExceptionTranslator().translate(task, sql, e);
    } else {
      return new SQLErrorCodeSQLExceptionTranslator(dataSource).translate(task,
        sql, e);
    }
  }

  public static String getProductName(final DataSource dataSource) {
    if (dataSource == null) {
      return null;
    } else {
      final Connection connection = getConnection(dataSource);
      try {
        if (connection == null) {
          if (dataSource.getClass().getName().toLowerCase().contains("oracle")) {
            return "Oracle";
          } else if (dataSource.getClass()
              .getName()
              .toLowerCase()
              .contains("postgres")) {
            return "PostgreSQL";
          } else {
            return null;
          }
        } else {
          final DatabaseMetaData metaData = connection.getMetaData();
          return metaData.getDatabaseProductName();
        }
      } catch (final SQLException e) {
        throw new IllegalArgumentException(
          "Unable to get database product name", e);
      } finally {
        release(connection, dataSource);
      }
    }
  }

  public static String getQualifiedTableName(final String typePath) {
    if (Property.hasValue(typePath)) {
      final String tableName = typePath.replaceAll("^/+", "");
      return tableName.replaceAll("/", ".");
    } else {
      return null;
    }
  }

  public static String getSchemaName(final String typePath) {
    if (Property.hasValue(typePath)) {
      final String path = Path.getPath(typePath);
      return path.replaceAll("(^/|/$)", "");
    } else {
      return "";
    }
  }

  public static String getSelectSql(final Query query) {
    final String tableName = query.getTypeName();
    final String dbTableName = getQualifiedTableName(tableName);

    String sql = query.getSql();
    final Map<String, Boolean> orderBy = query.getOrderBy();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (sql == null) {
      if (recordDefinition == null) {
        recordDefinition = new RecordDefinitionImpl(tableName);
        // throw new IllegalArgumentException("Unknown table name " +
        // tableName);
      }
      final List<String> fieldNames = new ArrayList<String>(
          query.getFieldNames());
      if (fieldNames.isEmpty()) {
        final List<String> recordDefinitionFieldNames = recordDefinition.getFieldNames();
        if (recordDefinitionFieldNames.isEmpty()) {
          fieldNames.add("T.*");
        } else {
          fieldNames.addAll(recordDefinitionFieldNames);
        }
      }
      final String fromClause = query.getFromClause();
      final boolean lockResults = query.isLockResults();
      sql = createSelectSql(recordDefinition, "T", fromClause, lockResults,
        fieldNames, query, orderBy);
    } else {
      if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
        final StringBuilder newSql = new StringBuilder("SELECT ");
        addColumnNames(newSql, recordDefinition, dbTableName);
        newSql.append(" FROM ");
        newSql.append(sql.substring(14));
        sql = newSql.toString();
      }
      if (!orderBy.isEmpty()) {
        final StringBuilder buffer = new StringBuilder(sql);
        addOrderBy(buffer, orderBy);
        sql = buffer.toString();
      }
    }
    return sql;
  }

  public static String getTableName(final String typePath) {
    final String tableName = Path.getName(typePath);
    return tableName;
  }

  public static void lockTable(final Connection connection,
    final String tableName) throws SQLException {
    final String sql = "LOCK TABLE " + tableName + " IN SHARE MODE";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.execute();
    } finally {
      close(statement);
    }
  }

  public static void lockTable(final RecordStore recordStore,
    final String typePath) {
    if (recordStore instanceof JdbcRecordStore) {
      final JdbcRecordStore jdbcRecordStore = (JdbcRecordStore)recordStore;
      try (
          final JdbcConnection connection = jdbcRecordStore.getJdbcConnection()) {
        final String tableName = getQualifiedTableName(typePath);
        final String sql = "LOCK TABLE " + tableName + " IN SHARE MODE";
        executeUpdate(connection, sql);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to lock table " + typePath, e);
      }
    }

  }

  public static Map<String, Object> readMap(final ResultSet rs)
      throws SQLException {
    final Map<String, Object> values = new LinkedHashMap<String, Object>();
    final ResultSetMetaData metaData = rs.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      final String name = metaData.getColumnName(i);
      final Object value = rs.getObject(i);
      values.put(name, value);
    }
    return values;
  }

  public static void release(final Connection connection,
    final DataSource dataSource) {
    if (dataSource != null && connection != null) {
      DataSourceUtils.releaseConnection(connection, dataSource);
    }
  }

  public static Date selectDate(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return resultSet.getDate(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static Date selectDate(final DataSource dataSource,
    final Connection connection, final String sql, final Object... parameters)
        throws SQLException {
    if (dataSource == null) {
      return selectDate(connection, sql, parameters);
    } else {
      return selectDate(dataSource, sql, parameters);
    }
  }

  public static Date selectDate(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectDate(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static int selectInt(final Connection connection, final String sql,
    final Object... parameters) {
    return selectInt(null, connection, sql, parameters);
  }

  public static int selectInt(final DataSource dataSource,
    Connection connection, final String sql, final Object... parameters) {
    if (dataSource != null) {
      connection = getConnection(dataSource);
    }
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        setParameters(statement, parameters);
        final ResultSet resultSet = statement.executeQuery();
        try {
          if (resultSet.next()) {
            return resultSet.getInt(1);
          } else {
            throw new IllegalArgumentException("Value not found");
          }
        } finally {
          close(resultSet);
        }

      } finally {
        close(statement);
      }
    } catch (final SQLException e) {
      throw getException(dataSource, connection, "selectInt", sql, e);
    } finally {
      if (dataSource != null) {
        release(connection, dataSource);
      }
    }
  }

  public static int selectInt(final DataSource dataSource, final String sql,
    final Object... parameters) {
    return selectInt(dataSource, null, sql, parameters);

  }

  public static int selectInt(final JdbcRecordStore recordStore,
    final String sql, final Object... parameters) {
    try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
      try (
          final PreparedStatement statement = connection.prepareStatement(sql)) {
        setParameters(statement, parameters);

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

  public static <T> List<T> selectList(final Connection connection,
    final String sql, final int columnIndex, final Object... parameters)
        throws SQLException {
    final List<T> results = new ArrayList<T>();
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          @SuppressWarnings("unchecked")
          final T value = (T)resultSet.getObject(columnIndex);
          results.add(value);
        }
        return results;
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static long selectLong(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return resultSet.getLong(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static long selectLong(final DataSource dataSource,
    final Connection connection, final String sql, final Object... parameters)
        throws SQLException {
    if (dataSource == null) {
      return selectLong(connection, sql, parameters);
    } else {
      return selectLong(dataSource, sql, parameters);
    }
  }

  public static long selectLong(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectLong(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static long selectLong(final JdbcRecordStore recordStore,
    final String sql, final Object... parameters) {
    try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
      try (
          final PreparedStatement statement = connection.prepareStatement(sql)) {
        setParameters(statement, parameters);

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

  public static Map<String, Object> selectMap(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return readMap(resultSet);
        } else {
          throw new IllegalArgumentException("Value not found for " + sql + " "
              + Arrays.asList(parameters));
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static Map<String, Object> selectMap(final DataSource dataSource,
    final String sql, final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectMap(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static Map<String, Object> selectMap(
    final JdbcRecordStore recordStore, final String sql,
    final Object... parameters) {
    try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
      try (
          final PreparedStatement statement = connection.prepareStatement(sql)) {
        setParameters(statement, parameters);

        try (
            final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return readMap(resultSet);
          } else {
            throw new IllegalArgumentException("Value not found for " + sql
              + " " + Arrays.asList(parameters));
          }
        }
      } catch (final SQLException e) {
        throw connection.getException(null, sql, e);
      }
    }
  }

  public static String selectString(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
      setParameters(statement, parameters);
      try (
          final ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      }
    }
  }

  public static String selectString(final DataSource dataSource,
    final Connection connection, final String sql, final Object... parameters)
        throws SQLException {
    if (dataSource == null) {
      return selectString(connection, sql, parameters);
    } else {
      return selectString(dataSource, sql, parameters);
    }
  }

  public static String selectString(final DataSource dataSource,
    final String sql, final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectString(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static String selectString(final JdbcRecordStore recordStore,
    final String sql, final Object... parameters) throws SQLException {
    try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
      return selectString(connection, sql, parameters);
    }
  }

  public static void setParameters(final PreparedStatement statement,
    final Object... parameters) throws SQLException {
    int index = 1;
    for (final Object parameter : parameters) {
      index = setValue(statement, index, parameter);
    }
  }

  public static void setPreparedStatementParameters(
    final PreparedStatement statement, final Query query) {
    int index = 1;
    for (final Object parameter : query.getParameters()) {
      final JdbcFieldDefinition attribute = JdbcFieldDefinition.createAttribute(parameter);
      try {
        index = attribute.setPreparedStatementValue(statement, index, parameter);
      } catch (final SQLException e) {
        throw new RuntimeException("Error setting value:" + parameter, e);
      }
    }
    final Condition where = query.getWhereCondition();
    if (where != null) {
      where.appendParameters(index, statement);
    }
  }

  public static int setValue(final PreparedStatement statement,
    final int index, final Object value) throws SQLException {
    final JdbcFieldDefinition attribute = JdbcFieldDefinition.createAttribute(value);
    return attribute.setPreparedStatementValue(statement, index, value);
  }

  private static final Logger LOG = Logger.getLogger(JdbcUtils.class);

  private JdbcUtils() {

  }

  public static BigDecimal[] getBigDecimalArray(final ResultSet resultSet,
    final int index) throws SQLException {
    final Array array = resultSet.getArray(index);
    return (BigDecimal[])array.getArray();
  }
}
