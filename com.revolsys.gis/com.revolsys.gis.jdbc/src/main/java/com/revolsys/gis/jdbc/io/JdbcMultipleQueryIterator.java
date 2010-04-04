package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

  private int currentQuery = -1;

  private final DataObjectFactory dataObjectFactory;

  private final DataSource dataSource;

  private final JdbcDataObjectStore dataStore;

  private final int fetchSize;

  private boolean hasNext = false;

  private DataObjectMetaData metaData;

  private final List<JdbcQuery> queries;

  private ResultSet resultSet;

  private PreparedStatement statement;

  public JdbcMultipleQueryIterator(
    final JdbcDataObjectStore dataStore,
    final List<JdbcQuery> queries,
    final boolean autoCommit,
    final int fetchSize) {
    super();
    this.connection = dataStore.getConnection();
    this.dataSource = dataStore.getDataSource();

    if (dataSource != null) {
      try {
        this.connection = dataSource.getConnection();
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

  public void close() {
    JdbcUtils.close(statement, resultSet);
    if (dataSource != null) {
      JdbcUtils.close(connection);
    }
  }

  protected String getErrorMessage() {
    return queries.get(currentQuery).getSql();
  }

  protected DataObjectMetaData getMetaData() {
    return metaData;
  }

  protected ResultSet getNextResultSet() {
    String sql = null;
    try {
      do {
        if (++currentQuery < queries.size()) {
          JdbcUtils.close(statement, resultSet);
          final JdbcQuery query = queries.get(currentQuery);
          sql = query.getSql();
          try {
            statement = connection.prepareStatement(sql);
            statement.setFetchSize(fetchSize);

            query.setPreparedStatementParameters(statement);

            resultSet = statement.executeQuery();
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            final QName tableName = query.getTableName();
            if (sql.toUpperCase().startsWith("SELECT * FROM ")) {
              this.metaData = dataStore.getMetaData(tableName);
            } else {
              this.metaData = dataStore.getMetaData(tableName,
                resultSetMetaData);
            }

            final QName typeName = query.getTypeName();
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
    if (resultSet == null) {
      resultSet = getNextResultSet();
      if (this.resultSet == null) {
        hasNext = false;
        close();
      } else {
        hasNext = true;
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
        for (final Attribute attribute : metaData.getAttributes()) {
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
      throw new RuntimeException(getErrorMessage(), e);
    }
  }

  public void remove() {
    throw new IllegalStateException("remove not supported");

  }
}
