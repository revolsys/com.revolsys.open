package com.revolsys.jdbc;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

public final class JdbcUtils {
  private static final Logger LOG = Logger.getLogger(JdbcUtils.class);

  public static String cleanObjectName(final String objectName) {
    return objectName.replaceAll("[^a-zA-Z\\._]", "");
  }

  public static void close(final Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (final SQLException e) {
        LOG.debug("SQL error closing connection", e);
      } catch (final Throwable e) {
        LOG.debug("Unknown error closing connection", e);
      }
    }
  }

  public static void close(
    final Connection connection,
    final Statement statement) {
    close(statement);
    close(connection);
  }

  public static void close(
    final Connection connection,
    final Statement statement,
    final ResultSet resultSet) {
    close(resultSet);
    close(statement);
    close(connection);
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

  public static void delete(
    final Connection connection,
    final String tableName,
    final String idColumn,
    final Object id) {

    final String sql = "DELETE FROM " + cleanObjectName(tableName) + " WHERE "
      + cleanObjectName(idColumn) + " = ?";
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        statement.setObject(1, id);
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

  public static int executeUpdate(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);

      }
      return statement.executeUpdate();
    } finally {
      close(statement);
    }
  }

  public static int executeUpdate(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return executeUpdate(connection, sql, parameters);
    } finally {
      close(connection);
    }
  }

  public static Connection getConnection(final DataSource dataSource) {
    try {
      return dataSource.getConnection();
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "SQL error getting connection from data source", e);
    } catch (final Throwable e) {
      throw new RuntimeException(
        "Unknown error getting connection from data source ", e);
    }
  }

  public static String getProductName(final DataSource dataSource) {
    final Connection connection = JdbcUtils.getConnection(dataSource);
    try {
      final DatabaseMetaData metaData = connection.getMetaData();
      return metaData.getDatabaseProductName();
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to get database product name",
        e);
    } finally {
      close(connection);
    }
  }

  public static String getTableName(final QName typeName) {
    if (typeName == null) {
      return null;
    } else {
      final String namespaceURI = typeName.getNamespaceURI();
      final String localPart = typeName.getLocalPart();
      if (namespaceURI == "") {
        return localPart;
      } else {
        return namespaceURI + "." + localPart;
      }
    }
  }

  public static void lockTable(
    final Connection connection,
    final String tableName) throws SQLException {
    final String sql = "LOCK TABLE " + tableName + " IN EXCLUSIVE MODE";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.execute();
    } finally {
      close(statement);
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

  public static Date selectDate(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
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

  public static Date selectDate(
    final DataSource dataSource,
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return JdbcUtils.selectDate(connection, sql, parameters);
    } else {
      return JdbcUtils.selectDate(dataSource, sql, parameters);
    }
  }

  public static Date selectDate(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectDate(connection, sql, parameters);
    } finally {
      close(connection);
    }
  }

  public static int selectInt(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
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
  }

  public static int selectInt(
    final DataSource dataSource,
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return JdbcUtils.selectInt(connection, sql, parameters);
    } else {
      return JdbcUtils.selectInt(dataSource, sql, parameters);
    }
  }

  public static int selectInt(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) {
    final Connection connection = getConnection(dataSource);
    try {
      return selectInt(connection, sql, parameters);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to execute " + sql + " "
        + Arrays.toString(parameters), e);
    } finally {
      close(connection);
    }
  }

  public static <T> List<T> selectList(
    final Connection connection,
    final String sql,
    final int columnIndex,
    final Object... parameters) throws SQLException {
    final List<T> results = new ArrayList<T>();
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
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

  public static long selectLong(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
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

  public static long selectLong(
    final DataSource dataSource,
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return JdbcUtils.selectLong(connection, sql, parameters);
    } else {
      return JdbcUtils.selectLong(dataSource, sql, parameters);
    }
  }

  public static long selectLong(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectLong(connection, sql, parameters);
    } finally {
      close(connection);
    }
  }

  public static Map<String, Object> selectMap(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
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

  public static Map<String, Object> selectMap(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectMap(connection, sql, parameters);
    } finally {
      close(connection);
    }
  }

  public static String selectString(
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        statement.setObject(i + 1, parameter);
      }
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return resultSet.getString(1);
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

  public static String selectString(
    final DataSource dataSource,
    final Connection connection,
    final String sql,
    final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return JdbcUtils.selectString(connection, sql, parameters);
    } else {
      return JdbcUtils.selectString(dataSource, sql, parameters);
    }
  }

  public static String selectString(
    final DataSource dataSource,
    final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectString(connection, sql, parameters);
    } finally {
      close(connection);
    }
  }

  private JdbcUtils() {

  }
}
