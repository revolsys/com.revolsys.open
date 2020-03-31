package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;

public class PostgreSQLJdbcQueryResultPager extends JdbcQueryResultPager {

  private Integer numResults;

  private List<Record> results = null;

  public PostgreSQLJdbcQueryResultPager(final JdbcRecordStore recordStore,
    final Map<String, Object> properties, final Query query) {
    super(recordStore, properties, query);
  }

  @Override
  public List<Record> getList() {
    synchronized (this) {
      if (this.results == null) {
        final ArrayList<Record> results = new ArrayList<>();
        final int pageSize = getPageSize();
        final int pageNumber = getPageNumber();
        if (pageNumber != -1) {
          String sql = getSql();

          final int startRowNum = (pageNumber - 1) * pageSize;
          sql = getSql() + " OFFSET " + startRowNum + " LIMIT " + pageSize;

          final RecordDefinition recordDefinition = getRecordDefinition();
          if (recordDefinition != null) {
            final RecordFactory recordFactory = getRecordFactory();
            final JdbcRecordStore recordStore = getRecordStore();
            try (
              JdbcConnection connection = recordStore.getJdbcConnection()) {

              try (
                final PreparedStatement statement = connection.prepareStatement(sql);
                final ResultSet resultSet = recordStore.getResultSet(statement, getQuery());) {
                if (resultSet.next()) {
                  int i = 0;
                  do {
                    final Record records = JdbcQueryIterator.getNextRecord(recordStore,
                      recordDefinition, this.fields, recordFactory, resultSet, this.internStrings);
                    results.add(records);
                    i++;
                  } while (resultSet.next() && i < pageSize);
                }
              } catch (final SQLException e) {
                throw connection.getException("updateResults", sql, e);
              }
            }
          }
        }
        this.results = results;
      }
      return this.results;
    }
  }

  @Override
  public int getNumResults() {
    if (this.numResults == null) {
      final JdbcRecordStore recordStore = getRecordStore();
      final Query query = getQuery();
      this.numResults = recordStore.getRecordCount(query);
      updateNumPages();
    }
    return this.numResults;
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    this.results = null;
  }
}
