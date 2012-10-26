package com.revolsys.jdbc;

import java.math.BigInteger;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public final class JdbcUtils {
  private static final Logger LOG = Logger.getLogger(JdbcUtils.class);

  public static void addAttributeName(final StringBuffer sql,
    final String tablePrefix, final Attribute attribute) {
    if (attribute instanceof JdbcAttribute) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      jdbcAttribute.addColumnName(sql, tablePrefix);
    } else {
      sql.append(attribute.getName());
    }
  }

  public static void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(i);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  public static void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final List<String> attributeNames, boolean hasColumns) {
    for (final String attributeName : attributeNames) {
      if (hasColumns) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(attributeName);
      if (attribute == null) {
        sql.append(attributeName);
      } else {
        addAttributeName(sql, tablePrefix, attribute);
      }
      hasColumns = true;
    }
  }

  public static void addOrderBy(final StringBuffer sql,
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

  public static String createSelectSql(final DataObjectMetaData metaData,
    final String tablePrefix, final String fromClause,
    final boolean lockResults, final List<String> attributeNames,
    final Map<String, ? extends Object> filter, String where,
    final Map<String, Boolean> orderBy) {
    final String typePath = metaData.getPath();
    final StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    boolean hasColumns = false;
    if (attributeNames.isEmpty() || attributeNames.remove("*")) {
      addColumnNames(sql, metaData, tablePrefix);
      hasColumns = true;
    }
    addColumnNames(sql, metaData, tablePrefix, attributeNames, hasColumns);
    sql.append(" FROM ");
    if (StringUtils.hasText(fromClause)) {
      sql.append(fromClause);
    } else {
      final String tableName = getQualifiedTableName(typePath);
      sql.append(tableName);
      sql.append(" ");
      sql.append(tablePrefix);
    }
    if (filter != null && !filter.isEmpty()) {
      final StringBuffer filterWhere = new StringBuffer();
      boolean first = true;
      for (final Entry<String, ?> entry : filter.entrySet()) {
        if (first) {
          first = false;
        } else {
          filterWhere.append(" AND ");
        }
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value == null) {
          filterWhere.append(key);
          filterWhere.append(" IS NULL");
        } else {
          final Attribute attribute = metaData.getAttribute(key);
          if (attribute instanceof JdbcAttribute) {
            final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;

            if (value instanceof Collection) {
              final Collection<?> collection = (Collection<?>)value;
              final int size = collection.size();
              filterWhere.append(key);
              filterWhere.append(" IN (");

              for (int i = 0; i < size; i++) {
                if (i > 0) {
                  filterWhere.append(", ");
                }
                jdbcAttribute.addInsertStatementPlaceHolder(filterWhere, false);
              }
              filterWhere.append(")");
            } else {
              filterWhere.append(key);
              filterWhere.append(" = ");
              jdbcAttribute.addInsertStatementPlaceHolder(filterWhere, false);
            }
          } else if (value instanceof Collection) {
            final Collection<?> collection = (Collection<?>)value;
            final int size = collection.size();
            filterWhere.append(key);
            filterWhere.append(" IN (");

            for (int i = 0; i < size; i++) {
              if (i > 0) {
                filterWhere.append(", ?");
              } else {
                filterWhere.append("?");
              }
            }
            filterWhere.append(")");
          } else {
            filterWhere.append(key);
            filterWhere.append(" = ?");
          }
        }
      }
      if (filterWhere.length() > 0) {
        if (StringUtils.hasText(where)) {
          where = "(" + where + ") AND (" + filterWhere + ")";
        } else {
          where = filterWhere.toString();
        }
      }
    }
    if (StringUtils.hasText(where)) {
      sql.append(" WHERE ");
      sql.append(where);
    }
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

  public static int executeUpdate(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        if (parameter instanceof Date) {
          Date date = (Date)parameter;
          statement.setDate(i + 1, date);
        } else {
          statement.setObject(i + 1, parameter);
        }
      }
      return statement.executeUpdate();
    } finally {
      close(statement);
    }
  }

  public static int executeUpdate(final DataSource dataSource,
    final String sql, final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return executeUpdate(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static Connection getConnection(final DataSource dataSource) {
    try {
      return DataSourceUtils.getConnection(dataSource);
    } catch (final Throwable e) {
      throw new RuntimeException(
        "Unknown error getting connection from data source ", e);
    }
  }

  public static String getDeleteSql(final Query query) {
    final String tableName = query.getTypeName();
    final String dbTableName = getQualifiedTableName(tableName);
    final DataObjectMetaData metaData = query.getMetaData();

    final StringBuffer sql = new StringBuffer();
    sql.append("DELETE FROM ");
    sql.append(dbTableName);
    sql.append(" T ");
    String where = query.getWhereClause();
    final Map<String, Object> filter = query.getFilter();
    if (!filter.isEmpty()) {
      final StringBuffer filterWhere = new StringBuffer();
      boolean first = true;
      for (final Entry<String, ?> entry : filter.entrySet()) {
        if (first) {
          first = false;
        } else {
          filterWhere.append(" AND ");
        }
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value == null) {
          filterWhere.append(key);
          filterWhere.append(" IS NULL");
        } else {
          final Attribute attribute = metaData.getAttribute(key);
          if (attribute instanceof JdbcAttribute) {
            final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
            filterWhere.append(key);
            filterWhere.append(" = ");
            jdbcAttribute.addInsertStatementPlaceHolder(filterWhere, false);
          } else {
            filterWhere.append(key);
            filterWhere.append(" = ?");
          }
        }
      }
      if (filterWhere.length() > 0) {
        if (StringUtils.hasText(where)) {
          where = "(" + where + ") AND (" + filterWhere + ")";
        } else {
          where = filterWhere.toString();
        }
      }
    }
    if (StringUtils.hasText(where)) {
      sql.append(" WHERE ");
      sql.append(where);
    }
    return sql.toString();
  }

  public static String getProductName(final DataSource dataSource) {
    final Connection connection = getConnection(dataSource);
    try {
      final DatabaseMetaData metaData = connection.getMetaData();
      return metaData.getDatabaseProductName();
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to get database product name",
        e);
    } finally {
      release(connection, dataSource);
    }
  }

  public static String getQualifiedTableName(final String typePath) {
    if (StringUtils.hasText(typePath)) {
      final String tableName = typePath.replaceAll("^/+", "");
      return tableName.replaceAll("/", ".");
    } else {
      return null;
    }
  }

  public static String getSchemaName(final String typePath) {
    if (StringUtils.hasText(typePath)) {
      final String path = PathUtil.getPath(typePath);
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
    final DataObjectMetaData metaData = query.getMetaData();
    if (sql == null) {
      if (metaData == null) {
        throw new IllegalArgumentException("Unknown table name " + tableName);
      }
      final List<String> attributeNames = new ArrayList<String>(
        query.getAttributeNames());
      final String fromClause = query.getFromClause();
      final String where = query.getWhereClause();
      final Map<String, ? extends Object> filter = query.getFilter();
      final boolean lockResults = query.isLockResults();
      sql = createSelectSql(metaData, "T", fromClause, lockResults,
        attributeNames, filter, where, orderBy);
      query.setSql(sql);
    } else {
      if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
        final StringBuffer newSql = new StringBuffer("SELECT ");
        addColumnNames(newSql, metaData, dbTableName);
        newSql.append(" FROM ");
        newSql.append(sql.substring(14));
        sql = newSql.toString();
        query.setSql(sql);
      }
      if (!orderBy.isEmpty()) {
        final StringBuffer buffer = new StringBuffer(sql);
        addOrderBy(buffer, orderBy);
        sql = buffer.toString();
      }
    }
    return sql;
  }

  public static String getTableName(final String typePath) {
    final String tableName = PathUtil.getName(typePath);
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

  public static int selectInt(final DataSource dataSource,
    final Connection connection, final String sql, final Object... parameters)
    throws SQLException {
    if (dataSource == null) {
      return selectInt(connection, sql, parameters);
    } else {
      return selectInt(dataSource, sql, parameters);
    }
  }

  public static int selectInt(final DataSource dataSource, final String sql,
    final Object... parameters) {
    final Connection connection = getConnection(dataSource);
    try {
      return selectInt(connection, sql, parameters);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to execute " + sql + " "
        + Arrays.toString(parameters), e);
    } finally {
      release(connection, dataSource);
    }
  }

  public static <T> List<T> selectList(final Connection connection,
    final String sql, final int columnIndex, final Object... parameters)
    throws SQLException {
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

  public static Map<String, Object> selectMap(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        if (parameter instanceof BigInteger) {
          final BigInteger bigInt = (BigInteger)parameter;
          statement.setLong(i + 1, bigInt.longValue());
        } else {
          statement.setObject(i + 1, parameter);
        }
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

  public static Map<String, Object> selectMap(final DataSource dataSource,
    final String sql, final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectMap(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static String selectString(final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
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

  public static int setPreparedStatementFilterParameters(
    final DataObjectMetaData metaData, final PreparedStatement statement,
    int parameterIndex, final Map<String, ? extends Object> filter)
    throws SQLException {
    if (filter != null && !filter.isEmpty()) {
      for (final Entry<String, ?> entry : filter.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value != null) {
          final Attribute attribute = metaData.getAttribute(key);
          JdbcAttribute jdbcAttribute;
          if (attribute instanceof JdbcAttribute) {
            jdbcAttribute = (JdbcAttribute)attribute;

          } else {
            jdbcAttribute = new JdbcAttribute();
          }
          if (value instanceof Collection) {
            final Collection<?> collection = (Collection<?>)value;
            for (final Object item : collection) {
              parameterIndex = jdbcAttribute.setPreparedStatementValue(
                statement, parameterIndex, item);
            }
          } else {
            parameterIndex = jdbcAttribute.setPreparedStatementValue(statement,
              parameterIndex, value);
          }
        }
      }
    }
    return parameterIndex;
  }

  public static void setPreparedStatementFilterParameters(
    final PreparedStatement statement, final Query query) throws SQLException {
    final DataObjectMetaData metaData = query.getMetaData();
    final Map<String, ? extends Object> filter = query.getFilter();
    int parameterIndex = setPreparedStatementParameters(query, statement);
    parameterIndex = setPreparedStatementFilterParameters(metaData, statement,
      parameterIndex, filter);
  }

  public static int setPreparedStatementParameters(final Query query,
    final PreparedStatement statement) throws SQLException {
    final List<Object> parameters = query.getParameters();
    final List<Attribute> parameterAttributes = query.getParameterAttributes();

    int statementParameterIndex = 1;
    for (int i = 0; i < parameters.size(); i++) {
      final Attribute attribute = parameterAttributes.get(i);
      JdbcAttribute jdbcAttribute;
      if (attribute instanceof JdbcAttribute) {
        jdbcAttribute = (JdbcAttribute)attribute;

      } else {
        jdbcAttribute = new JdbcAttribute();
      }
      final Object value = parameters.get(i);
      statementParameterIndex = jdbcAttribute.setPreparedStatementValue(
        statement, statementParameterIndex, value);
    }
    return statementParameterIndex;
  }

  private JdbcUtils() {

  }
}
