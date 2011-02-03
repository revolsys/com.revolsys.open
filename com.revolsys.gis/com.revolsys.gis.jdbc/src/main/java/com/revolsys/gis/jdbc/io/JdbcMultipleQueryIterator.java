package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcMultipleQueryIterator implements Iterator<DataObject> {
  private Connection connection;

  private int currentQueryIndex = -1;

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private final int fetchSize;

  private boolean hasNext = true;

  private DataObjectMetaData metaData;

  private List<JdbcQuery> queries;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  public JdbcMultipleQueryIterator(final JdbcDataObjectStore dataStore,
    final List<JdbcQuery> queries, final boolean autoCommit, final int fetchSize) {
    super();
    this.connection = dataStore.getConnection();
    this.dataSource = dataStore.getDataSource();

    if (dataSource != null) {
      try {
        this.connection = JdbcUtils.getConnection(dataSource);
        this.connection.setAutoCommit(autoCommit);
      } catch (final SQLException e) {
        throw new IllegalArgumentException("Unable to create connection", e);
      }
    }
    this.dataObjectFactory = dataStore.getDataObjectFactory();
    this.dataStore = dataStore;
    this.queries = queries;
    this.fetchSize = fetchSize;
  }

  @PreDestroy
  public void close() {
    JdbcUtils.close(statement, resultSet);
    if (dataSource != null) {
      JdbcUtils.close(connection);
    }
    hasNext = false;
    connection = null;
    dataObjectFactory = null;
    dataSource = null;
    dataStore = null;
    hasNext = false;
    metaData = null;
    queries = null;
    resultSet = null;
    statement = null;
  }

  protected String getErrorMessage() {
    return queries.get(currentQueryIndex).getSql();
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  protected ResultSet getNextResultSet() {
    String sql = null;
    try {
      do {
        if (++currentQueryIndex < queries.size()) {
          JdbcUtils.close(statement, resultSet);
          final JdbcQuery currentQuery = queries.get(currentQueryIndex);
          sql = currentQuery.getSql();
          try {
            final QName tableName = currentQuery.getTableName();
            DataObjectMetaData metaData = currentQuery.getMetaData();
            if (metaData == null) {
              if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
                metaData = dataStore.getMetaData(tableName);
                StringBuffer newSql = new StringBuffer("SELECT ");
                JdbcQuery.addColumnNames(newSql, metaData,
                  JdbcQuery.getTableName(tableName));
                newSql.append(" FROM ");
                newSql.append(sql.substring(14));
                sql = newSql.toString();
              }
            }
            statement = connection.prepareStatement(sql);
            statement.setFetchSize(fetchSize);

            currentQuery.setPreparedStatementParameters(statement);

            resultSet = statement.executeQuery();
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            if (metaData == null) {
              metaData = dataStore.getMetaData(tableName, resultSetMetaData);
            }
            this.metaData = metaData;
            List<String> attributeNames = currentQuery.getAttributeNames();
            if (attributeNames.isEmpty()) {
              this.attributes = metaData.getAttributes();
            }
            for (String attributeName : attributeNames) {
              Attribute attribute = metaData.getAttribute(attributeName);
              if (attribute != null) {
                attributes.add(attribute);
              }
            }

            final QName typeName = currentQuery.getTypeName();
            if (typeName != null) {
              final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl)metaData).clone();
              newMetaData.setName(typeName);
              this.metaData = newMetaData;
            }
          } catch (final SQLException e) {
            JdbcUtils.close(statement, resultSet);
            throw new RuntimeException("Error executig query:" + sql, e);
          }
        } else {
          return null;
        }
      } while (!resultSet.next());
    } catch (final SQLException e) {
      throw new RuntimeException("Error executig query:" + sql, e);
    }
    return resultSet;
  }

  public boolean hasNext() {
    if (hasNext && resultSet == null) {
      try {
        resultSet = getNextResultSet();
      } finally {
        if (this.resultSet == null) {
          hasNext = false;
          close();
        } else {
          hasNext = true;
        }
      }
    }
    return hasNext;
  }

  public DataObject next() {
    try {
      if (!hasNext) {
        throw new NoSuchElementException("No more elements");
      } else {
        final DataObjectMetaData metaData = getMetaData();
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
        if (!resultSet.next()) {
          try {
            resultSet.close();
          } finally {
            resultSet = null;
          }

        }
        return object;
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

  public void remove() {
    throw new IllegalStateException("remove not supported");

  }
}
