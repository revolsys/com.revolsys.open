package com.revolsys.jdbc.io;

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

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class JdbcQueryIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {

  public static DataObject getNextObject(final JdbcDataObjectStore dataStore,
    final DataObjectMetaData metaData, final List<Attribute> attributes,
    final DataObjectFactory dataObjectFactory, final ResultSet resultSet) {
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    if (object != null) {
      object.setState(DataObjectState.Initalizing);
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
    }
    return object;
  }

  public static ResultSet getResultSet(final DataObjectMetaData metaData,
    final PreparedStatement statement, final Query query) throws SQLException {
    JdbcUtils.setPreparedStatementFilterParameters(statement, query);

    return statement.executeQuery();
  }

  protected void setQuery(Query query) {
    this.query = query;
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

  private Statistics statistics;

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
    this.dataObjectFactory = query.getProperty("dataObjectFactory");
    if (this.dataObjectFactory == null) {
      this.dataObjectFactory = dataStore.getDataObjectFactory();
    }
    this.dataStore = dataStore;
    this.query = query;
    this.statistics = (Statistics)properties.get(Statistics.class.getName());
  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(statement, resultSet);
    JdbcUtils.release(connection, dataSource);
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
    statistics = null;
  }

  @Override
  protected void doInit() {
    this.resultSet = getResultSet();
  }

  protected String getErrorMessage() {
    if (queries == null) {
      return null;
    } else {
      return queries.get(currentQueryIndex).getSql();
    }
  }

  @Override
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
        if (statistics != null) {
          statistics.add(object);
        }
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

  protected ResultSet getResultSet() {
    final String tableName = query.getTypeName();
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

      final String typePath = query.getTypeNameAlias();
      if (typePath != null) {
        final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl)metaData).clone();
        newMetaData.setName(typePath);
        this.metaData = newMetaData;
      }
    } catch (final SQLException e) {
      JdbcUtils.close(statement, resultSet);
      throw new RuntimeException("Error executing query:" + sql, e);
    }
    return resultSet;
  }

  protected String getSql(final Query query) {
    return JdbcUtils.getSelectSql(query);
  }

}
