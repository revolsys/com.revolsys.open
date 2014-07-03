package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.revolsys.collection.ResultPager;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcQueryResultPager implements ResultPager<Record> {
  /** The objects in the current page. */
  private List<Record> results;

  /** The number of objects in a page. */
  private int pageSize = 10;

  /** The current page number. */
  private int pageNumber = -1;

  /** The total number of results. */
  private int numResults;

  /** The number of pages. */
  private int numPages;

  private Connection connection;

  private DataSource dataSource;

  private RecordFactory dataObjectFactory;

  private JdbcDataObjectStore dataStore;

  private RecordDefinition metaData;

  private PreparedStatement statement;

  private ResultSet resultSet;

  private final Query query;

  private final String sql;

  public JdbcQueryResultPager(final JdbcDataObjectStore dataStore,
    final Map<String, Object> properties, final Query query) {
    this.connection = dataStore.getConnection();
    this.dataSource = dataStore.getDataSource();

    if (dataSource != null) {
      try {
        this.connection = JdbcUtils.getConnection(dataSource);
        boolean autoCommit = false;
        if (BooleanStringConverter.getBoolean(properties.get("autoCommit"))) {
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

    final String tableName = query.getTypeName();
    metaData = query.getMetaData();
    if (metaData == null) {
      metaData = dataStore.getRecordDefinition(tableName);
      query.setMetaData(metaData);
    }

    this.sql = JdbcUtils.getSelectSql(query);
  }

  @Override
  @PreDestroy
  public void close() {
    JdbcUtils.close(statement, resultSet);
    JdbcUtils.release(connection, dataSource);
    connection = null;
    dataObjectFactory = null;
    dataSource = null;
    dataStore = null;
    metaData = null;
    results = null;
    resultSet = null;
    statement = null;

  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  public Connection getConnection() {
    return connection;
  }

  public RecordFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  /**
   * Get the index of the last object in the current page.
   * 
   * @return The index of the last object in the current page.
   */
  @Override
  public int getEndIndex() {
    if (pageNumber == getNumPages()) {
      return getNumResults();
    } else {
      return (pageNumber + 1) * pageSize;
    }
  }

  /**
   * Get the list of objects in the current page.
   * 
   * @return The list of objects in the current page.
   */
  @Override
  public List<Record> getList() {
    if (results == null) {
      throw new IllegalStateException(
        "The page number must be set using setPageNumber");
    }
    return results;
  }

  public RecordDefinition getMetaData() {
    return metaData;
  }

  /**
   * Get the page number of the next page.
   * 
   * @return Thepage number of the next page.
   */
  @Override
  public int getNextPageNumber() {
    return pageNumber + 2;
  }

  /**
   * Get the number of pages.
   * 
   * @return The number of pages.
   */
  @Override
  public int getNumPages() {
    return numPages + 1;
  }

  /**
   * Get the total number of results returned.
   * 
   * @return The total number of results returned.
   */
  @Override
  public int getNumResults() {
    return numResults;
  }

  /**
   * Get the page number of the current page. Index starts at 1.
   * 
   * @return The page number of the current page.
   */
  @Override
  public int getPageNumber() {
    return pageNumber + 1;
  }

  /**
   * Get the number of objects to display in a page.
   * 
   * @return The number of objects to display in a page.
   */
  @Override
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Get the page number of the previous page.
   * 
   * @return Thepage number of the previous page.
   */
  @Override
  public int getPreviousPageNumber() {
    return pageNumber;
  }

  public Query getQuery() {
    return query;
  }

  protected String getSql() {
    return sql;
  }

  /**
   * Get the index of the first object in the current page. Index starts at 1.
   * 
   * @return The index of the first object in the current page.
   */
  @Override
  public int getStartIndex() {
    if (getNumResults() == 0) {
      return 0;
    } else {
      return (pageNumber * pageSize) + 1;
    }
  }

  /**
   * Check to see if there is a next page.
   * 
   * @return True if there is a next page.
   */
  @Override
  public boolean hasNextPage() {
    return pageNumber < getNumPages();
  }

  /**
   * Check to see if there is a previous page.
   * 
   * @return True if there is a previous page.
   */
  @Override
  public boolean hasPreviousPage() {
    return pageNumber > 0;
  }

  private void initResultSet() {
    if (resultSet == null) {
      try {
        statement = connection.prepareStatement(sql,
          ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(pageSize);

        resultSet = JdbcQueryIterator.getResultSet(metaData, statement, query);
        resultSet.last();
        this.numResults = resultSet.getRow();
      } catch (final SQLException e) {
        JdbcUtils.close(statement, resultSet);
        throw new RuntimeException("Error executing query:" + sql, e);
      }
    }
  }

  public boolean isClosed() {
    return dataStore == null;
  }

  /**
   * Check to see if this is the first page.
   * 
   * @return True if this is the first page.
   */
  @Override
  public boolean isFirstPage() {
    return pageNumber == 0;
  }

  /**
   * Check to see if this is the last page.
   * 
   * @return True if this is the last page.
   */
  @Override
  public boolean isLastPage() {
    return pageNumber == getNumPages();
  }

  protected void setNumResults(final int numResults) {
    this.numResults = numResults;
  }

  /**
   * Set the current page number.
   * 
   * @param pageNumber The current page number.
   */
  @Override
  public void setPageNumber(final int pageNumber) {
    if (pageNumber > getNumPages()) {
      this.pageNumber = getNumPages() - 1;
    } else if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
    updateResults();
  }

  @Override
  public void setPageNumberAndSize(final int pageSize, final int pageNumber) {
    if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
    this.pageSize = pageSize;
    updateNumPages();
    updateResults();
  }

  /**
   * Set the number of objects per page.
   * 
   * @param pageSize The number of objects per page.
   */
  @Override
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
    updateNumPages();
    updateResults();
  }

  protected void setResults(final List<Record> results) {
    this.results = results;
  }

  protected void updateNumPages() {
    this.numPages = Math.max(0, ((getNumResults() - 1) / getPageSize()));
  }

  /**
   * Update the cached results for the current page.
   */
  protected void updateResults() {
    results = new ArrayList<Record>();
    try {
      initResultSet();
      if (pageNumber != -1 && resultSet != null) {
        if (resultSet.absolute(pageNumber * pageSize + 1)) {
          int i = 0;
          do {
            final Record object = JdbcQueryIterator.getNextObject(
              dataStore, metaData, metaData.getAttributes(), dataObjectFactory,
              resultSet);
            results.add(object);
            i++;
          } while (resultSet.next() && i < pageSize);
        }
      }
    } catch (final SQLException e) {
      JdbcUtils.getException(getDataSource(), getConnection(), "updateResults",
        sql, e);
    } catch (final RuntimeException e) {
      close();
    } catch (final Error e) {
      close();
    }
  }
}
