package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class JdbcQueryIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {
  public static void addAttributeName(
    final StringBuffer sql,
    final String tablePrefix,
    final Attribute attribute) {
    if (attribute instanceof JdbcAttribute) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      jdbcAttribute.addColumnName(sql, tablePrefix);
    } else {
      sql.append(attribute.getName());
    }
  }

  public static void addColumnNames(
    final StringBuffer sql,
    final DataObjectMetaData metaData,
    final String tablePrefix) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(i);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  public static void addColumnNames(
    final StringBuffer sql,
    final DataObjectMetaData metaData,
    final String tablePrefix,
    final List<String> attributeNames,
    boolean hasColumns) {
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

  public static String createSql(
    final DataObjectMetaData metaData,
    final String tablePrefix,
    final String fromClause,
    boolean lockResults,
    final List<String> attributeNames,
    final Map<String, ? extends Object> filter,
    String where,
    final Map<String, Boolean> orderBy) {
    final QName typeName = metaData.getName();
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
      final String tableName = JdbcUtils.getTableName(typeName);
      sql.append(tableName);
      sql.append(" ");
      sql.append(tablePrefix);
    }
    if (filter != null) {
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
    if (!orderBy.isEmpty()) {
      sql.append(" ORDER BY ");
      for (Iterator<Entry<String, Boolean>> iterator = orderBy.entrySet()
        .iterator(); iterator.hasNext();) {
        Entry<String, Boolean> entry = iterator.next();
        String column = entry.getKey();
        sql.append(column);
        Boolean ascending = entry.getValue();
        if (!ascending) {
          sql.append(" DESC");
        }
        if (iterator.hasNext()) {
          sql.append(", ");
        }
      }
    }
    if (lockResults) {
      sql.append(" FOR UPDATE");
    }
    return sql.toString();
  }

  public static DataObject getNextObject(
    final JdbcDataObjectStore dataStore,
    final DataObjectMetaData metaData,
    final List<Attribute> attributes,
    final DataObjectFactory dataObjectFactory,
    final ResultSet resultSet) {
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    int columnIndex = 1;

    for (final Attribute attribute : attributes) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      try {
        columnIndex = jdbcAttribute.setAttributeValueFromResultSet(resultSet,
          columnIndex, object);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to get value " + (columnIndex + 1)
          + " from result set", e);
      }
    }
    object.setState(DataObjectState.Persisted);
    dataStore.addStatistic("query", object);
    return object;
  }

  public static ResultSet getResultSet(
    final DataObjectMetaData metaData,
    final PreparedStatement statement,
    final Query query) throws SQLException {
    final Map<String, ? extends Object> filter = query.getFilter();
    int parameterIndex = setPreparedStatementParameters(query, statement);
    parameterIndex = setPreparedStatementFilterParameters(metaData, statement,
      parameterIndex, filter);

    return statement.executeQuery();
  }

  public static String getSql(final Query query) {
    final QName tableName = query.getTypeName();
    final String dbTableName = JdbcUtils.getTableName(tableName);

    String sql = query.getSql();
    final DataObjectMetaData metaData = query.getMetaData();
    if (sql == null) {
      if (metaData == null) {
        throw new IllegalArgumentException("Unknown table name " + tableName);
      }
      final List<String> attributeNames = new ArrayList<String>(
        query.getAttributeNames());
      final String fromClause = query.getFromClause();
      final String where = query.getWhereClause();
      final Map<String, Boolean> orderBy = query.getOrderBy();
      final Map<String, ? extends Object> filter = query.getFilter();
      boolean lockResults = query.isLockResults();
      sql = createSql(metaData, "T", fromClause, lockResults, attributeNames,
        filter, where, orderBy);
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
    }
    return sql;
  }

  public static int setPreparedStatementFilterParameters(
    final DataObjectMetaData metaData,
    final PreparedStatement statement,
    int parameterIndex,
    final Map<String, ? extends Object> filter) throws SQLException {
    if (filter != null) {
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
          parameterIndex = jdbcAttribute.setPreparedStatementValue(statement,
            parameterIndex, value);
        }
      }
    }
    return parameterIndex;
  }

  public static int setPreparedStatementParameters(
    final Query query,
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

  private Connection connection;

  private final int currentQueryIndex = -1;

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private final int fetchSize = 10;

  private DataObjectMetaData metaData;

  private List<Query> queries;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Query query;

  public JdbcQueryIterator(final JdbcDataObjectStore dataStore,
    final Query query, final Map<String, Object> properties) {
    super();
    this.connection = dataStore.getConnection();
    this.dataSource = dataStore.getDataSource();

    if (dataSource != null) {
      try {
        this.connection = JdbcUtils.getConnection(dataSource);
        boolean autoCommit = false;
        if (properties.get("autoCommit") == Boolean.TRUE) {
          autoCommit = true;
        }
        this.connection.setAutoCommit(autoCommit);
      } catch (final SQLException e) {
        throw new IllegalArgumentException("Unable to create connection", e);
      }
    }
    this.dataObjectFactory = dataStore.getDataObjectFactory();
    this.dataStore = dataStore;
    this.query = query;

  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(statement, resultSet);
    if (dataSource != null) {
      JdbcUtils.close(connection);
    }
    attributes = null;
    connection = null;
    dataObjectFactory = null;
    dataSource = null;
    dataStore = null;
    metaData = null;
    queries = null;
    query = null;
    resultSet = null;
    statement = null;
  }

  @Override
  protected void doInit() {
    this.resultSet = getResultSet(query);
  }

  protected String getErrorMessage() {
    return queries.get(currentQueryIndex).getSql();
  }

  public DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    try {
      if (resultSet != null && resultSet.next()) {
        final DataObject object = getNextObject(dataStore, metaData,
          attributes, dataObjectFactory, resultSet);
        return object;
      } else {
        close();
        throw new NoSuchElementException();
      }
    } catch (final SQLException e) {
      close();
      throw new RuntimeException(getErrorMessage(), e);
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  protected ResultSet getResultSet(final Query query) {
    final QName tableName = query.getTypeName();
    metaData = query.getMetaData();
    if (metaData == null) {
      if (tableName != null) {
        metaData = dataStore.getMetaData(tableName);
        query.setMetaData(metaData);
      }
    }
    final String sql = getSql(query);
    try {
      statement = connection.prepareStatement(sql);
      statement.setFetchSize(fetchSize);

      resultSet = getResultSet(metaData, statement, query);
      final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

      if (metaData == null) {
        metaData = dataStore.getMetaData(tableName, resultSetMetaData);
      }
      final List<String> attributeNames = new ArrayList<String>(
        query.getAttributeNames());
      if (attributeNames.isEmpty()) {
        this.attributes.addAll(metaData.getAttributes());
      } else {
        for (final String attributeName : attributeNames) {
          if (attributeName.equals("*")) {
            this.attributes.addAll(metaData.getAttributes());
          } else {
            final Attribute attribute = metaData.getAttribute(attributeName);
            if (attribute != null) {
              attributes.add(attribute);
            }
          }
        }
      }

      final QName typeName = query.getTypeNameAlias();
      if (typeName != null) {
        final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl)metaData).clone();
        newMetaData.setName(typeName);
        this.metaData = newMetaData;
      }
    } catch (final SQLException e) {
      JdbcUtils.close(statement, resultSet);
      throw new RuntimeException("Error executing query:" + sql, e);
    }
    return resultSet;
  }

}
