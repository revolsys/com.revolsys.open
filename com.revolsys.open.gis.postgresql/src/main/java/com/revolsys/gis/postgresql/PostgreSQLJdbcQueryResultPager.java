package com.revolsys.gis.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;

public class PostgreSQLJdbcQueryResultPager extends JdbcQueryResultPager {

  private Integer numResults;

  private List<DataObject> results = null;

  public PostgreSQLJdbcQueryResultPager(final JdbcDataObjectStore dataStore,
    final Map<String, Object> properties, final Query query) {
    super(dataStore, properties, query);
  }

  @Override
  public List<DataObject> getList() {
    synchronized (this) {
      if (results == null) {
        final ArrayList<DataObject> results = new ArrayList<DataObject>();
        final int pageSize = getPageSize();
        final int pageNumber = getPageNumber();
        if (pageNumber != -1) {
          String sql = getSql();
          System.out.println(sql);

          final int startRowNum = ((pageNumber - 1) * pageSize);
          sql = getSql() + " OFFSET " + startRowNum + " LIMIT " + pageSize;

          final DataSource dataSource = getDataSource();
          Connection connection = getConnection();
          if (dataSource != null) {
            connection = JdbcUtils.getConnection(dataSource);
          }
          try {
            final JdbcDataObjectStore dataStore = getDataStore();
            final DataObjectFactory dataObjectFactory = getDataObjectFactory();
            final DataObjectMetaData metaData = getMetaData();
            if (metaData != null) {
              final List<Attribute> attributes = metaData.getAttributes();

              final PreparedStatement statement = connection.prepareStatement(sql);
              try {
                final ResultSet resultSet = JdbcQueryIterator.getResultSet(
                  metaData, statement, getQuery());
                try {
                  if (resultSet.next()) {
                    int i = 0;
                    do {
                      final DataObject object = JdbcQueryIterator.getNextObject(
                        dataStore, metaData, attributes, dataObjectFactory,
                        resultSet);
                      results.add(object);
                      i++;
                    } while (resultSet.next() && i < pageSize);
                  }
                } finally {
                  JdbcUtils.close(resultSet);
                }
              } finally {
                JdbcUtils.close(statement);
              }
            }
          } catch (final SQLException e) {
            JdbcUtils.getException(dataSource, connection, "updateResults",
              sql, e);
          } finally {
            if (dataSource != null) {
              JdbcUtils.release(connection, dataSource);
            }
          }
        }
        this.results = results;
      }
      return results;
    }
  }

  @Override
  public int getNumResults() {
    if (numResults == null) {
      final JdbcDataObjectStore dataStore = getDataStore();
      final Query query = getQuery();
      numResults = dataStore.getRowCount(query);
      updateNumPages();
    }
    return numResults;
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    results = null;
  }
}
