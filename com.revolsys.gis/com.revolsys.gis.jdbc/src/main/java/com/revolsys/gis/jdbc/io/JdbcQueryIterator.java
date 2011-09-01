package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.io.Query;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcQueryIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {
  private Connection connection;

  private final int currentQueryIndex = -1;

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private int fetchSize = 10;

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

  private void addAttributeName(final StringBuffer sql,
    final String tablePrefix, final Attribute attribute) {
    if (attribute instanceof JdbcAttribute) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      jdbcAttribute.addColumnName(sql, tablePrefix);
    } else {
      sql.append(attribute.getName());
    }
  }

  private void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(i);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  private void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final List<String> attributeNames) {
    for (int i = 0; i < attributeNames.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final String attributeName = attributeNames.get(i);
      final Attribute attribute = metaData.getAttribute(attributeName);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  private String createSql(final DataObjectMetaData metaData,
    final String tablePrefix, final String fromClause,
    final List<String> attributeNames, final String where,
    final List<String> orderBy) {
    final QName typeName = metaData.getName();
    final StringBuffer sql = new StringBuffer();
    sql.append("SELECT ");
    if (attributeNames.isEmpty()) {
      addColumnNames(sql, metaData, tablePrefix);
    } else {
      addColumnNames(sql, metaData, tablePrefix, attributeNames);
    }
    sql.append(" FROM ");
    if (StringUtils.hasText(fromClause)) {
      sql.append(fromClause);
    } else {
      final String tableName = JdbcUtils.getTableName(typeName);
      sql.append(tableName);
      sql.append(" ");
      sql.append(tablePrefix);
    }
    if (StringUtils.hasText(where)) {
      sql.append(" WHERE ");
      sql.append(where);
    }
    if (!orderBy.isEmpty()) {
      sql.append(" ORDER BY ");
      for (int i = 0; i < orderBy.size(); i++) {
        if (i > 0) {
          sql.append(", ");
        }
        final String name = orderBy.get(i);
        sql.append(name);
      }
    }
    return sql.toString();
  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(statement, resultSet);
    if (dataSource != null) {
      JdbcUtils.close(connection);
    }
    connection = null;
    dataObjectFactory = null;
    dataSource = null;
    dataStore = null;
    metaData = null;
    resultSet = null;
    statement = null;
    query = null;
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
      if (resultSet.next()) {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        int columnIndex = 1;

        for (final Attribute attribute : attributes) {
          final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
          try {
            columnIndex = jdbcAttribute.setAttributeValueFromResultSet(
              resultSet, columnIndex, object);
          } catch (final SQLException e) {
            close();
            throw new RuntimeException("Unable to get value "
              + (columnIndex + 1) + " from result set", e);
          }
        }
        object.setState(DataObjectState.Persisted);

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
    final String dbTableName = JdbcUtils.getTableName(tableName);
    metaData = query.getMetaData();
    String sql = query.getSql();
    if (sql == null) {
      if (metaData == null) {
        metaData = dataStore.getMetaData(tableName);
      }
      final List<String> attributeNames = query.getAttributeNames();
      final String fromClause = query.getFromClause();
      final String where = query.getWhereClause();
      final List<String> orderBy = query.getOrderBy();
      sql = createSql(metaData, "T", fromClause, attributeNames, where, orderBy);
    } else {
      if (metaData == null) {
        if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
          metaData = dataStore.getMetaData(tableName);
          final StringBuffer newSql = new StringBuffer("SELECT ");
          addColumnNames(newSql, metaData, dbTableName);
          newSql.append(" FROM ");
          newSql.append(sql.substring(14));
          sql = newSql.toString();
        }
      }
    }
    try {
      statement = connection.prepareStatement(sql);
      statement.setFetchSize(fetchSize);

      setPreparedStatementParameters(query, statement);

      resultSet = statement.executeQuery();
      final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

      if (metaData == null) {
        metaData = dataStore.getMetaData(tableName, resultSetMetaData);
      }
      final List<String> attributeNames = query.getAttributeNames();
      if (attributeNames.isEmpty()) {
        this.attributes = metaData.getAttributes();
      }
      for (final String attributeName : attributeNames) {
        final Attribute attribute = metaData.getAttribute(attributeName);
        if (attribute != null) {
          attributes.add(attribute);
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

  public void setPreparedStatementParameters(final Query query,
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
  }

}
