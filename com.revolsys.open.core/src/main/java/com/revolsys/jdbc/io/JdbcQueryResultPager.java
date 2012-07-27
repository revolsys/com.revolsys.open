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
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.util.ExceptionUtil;

public class JdbcQueryResultPager implements ResultPager<DataObject> {
  /** The objects in the current page. */
  private List<DataObject> results;

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

  private DataObjectFactory dataObjectFactory;

  private JdbcDataObjectStore dataStore;

  private DataObjectMetaData metaData;

  private PreparedStatement statement;

  private ResultSet resultSet;

  public JdbcQueryResultPager(final JdbcDataObjectStore dataStore,
    final Map<String, Object> properties, final Query query) {
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

    init(query);
  }

  @PreDestroy
  public void close() {
    JdbcUtils.close(statement, resultSet);
    if (dataSource != null) {
      JdbcUtils.close(connection);
    }
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

  /**
   * Get the index of the last object in the current page.
   * 
   * @return The index of the last object in the current page.
   */
  public int getEndIndex() {
    if (pageNumber == numPages) {
      return numResults;
    } else {
      return (pageNumber + 1) * pageSize;
    }
  }

  /**
   * Get the list of objects in the current page.
   * 
   * @return The list of objects in the current page.
   */
  public List<DataObject> getList() {
    if (results == null) {
      throw new IllegalStateException(
        "The page number must be set using setPageNumber");
    }
    return results;
  }

  /**
   * Get the page number of the next page.
   * 
   * @return Thepage number of the next page.
   */
  public int getNextPageNumber() {
    return pageNumber + 2;
  }

  /**
   * Get the number of pages.
   * 
   * @return The number of pages.
   */
  public int getNumPages() {
    return numPages + 1;
  }

  /**
   * Get the total number of results returned.
   * 
   * @return The total number of results returned.
   */
  public int getNumResults() {
    return numResults;
  }

  /**
   * Get the page number of the current page.
   * 
   * @return Thepage number of the current page.
   */
  public int getPageNumber() {
    return pageNumber + 1;
  }

  /**
   * Get the number of objects to display in a page.
   * 
   * @return The number of objects to display in a page.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Get the page number of the previous page.
   * 
   * @return Thepage number of the previous page.
   */
  public int getPreviousPageNumber() {
    return pageNumber;
  }

  /**
   * Get the index of the first object in the current page.
   * 
   * @return The index of the first object in the current page.
   */
  public int getStartIndex() {
    return (pageNumber * pageSize) + 1;
  }

  /**
   * Check to see if there is a next page.
   * 
   * @return True if there is a next page.
   */
  public boolean hasNextPage() {
    return pageNumber < numPages;
  }

  /**
   * Check to see if there is a previous page.
   * 
   * @return True if there is a previous page.
   */
  public boolean hasPreviousPage() {
    return pageNumber > 0;
  }

  protected void init(final Query query) {
    final String tableName = query.getTypeName();
    metaData = query.getMetaData();
    if (metaData == null) {
      metaData = dataStore.getMetaData(tableName);
      query.setMetaData(metaData);
    }

    final String sql = JdbcUtils.getSelectSql(query);

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

  /**
   * Check to see if this is the first page.
   * 
   * @return True if this is the first page.
   */
  public boolean isFirstPage() {
    return pageNumber == 0;
  }

  /**
   * Check to see if this is the last page.
   * 
   * @return True if this is the last page.
   */
  public boolean isLastPage() {
    return pageNumber == numPages;
  }

  /**
   * Set the current page number.
   * 
   * @param pageNumber The current page number.
   */
  public void setPageNumber(final int pageNumber) {
    if (pageNumber - 1 > numPages) {
      this.pageNumber = numPages;
    } else if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
    updateResults();
  }

  /**
   * Set the number of objects per page.
   * 
   * @param pageSize The number of objects per page.
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
    this.numPages = Math.max(0, ((numResults - 1) / pageSize));
    updateResults();
  }

  /**
   * Update the cached results for the current page.
   */
  private void updateResults() {
    results = new ArrayList<DataObject>();
    try {
      if (pageNumber != -1 && resultSet != null) {
        if (resultSet.absolute(pageNumber * pageSize + 1)) {
          int i = 0;
          do {
            final DataObject object = JdbcQueryIterator.getNextObject(
              dataStore, metaData, metaData.getAttributes(), dataObjectFactory,
              resultSet);
            results.add(object);
            i++;
          } while (resultSet.next() && i < pageSize);
        }
      }
    } catch (final Throwable t) {
      close();
      ExceptionUtil.throwUncheckedException(t);
    }
  }
}
