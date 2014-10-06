package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;
import com.revolsys.jdbc.io.JdbcRecordStore;

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
        final ArrayList<Record> results = new ArrayList<Record>();
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
              final List<FieldDefinition> attributes = recordDefinition.getFields();

              try (
                  final PreparedStatement statement = connection.prepareStatement(sql);
                  final ResultSet resultSet = JdbcQueryIterator.getResultSet(
                    recordDefinition, statement, getQuery());) {
                if (resultSet.next()) {
                  int i = 0;
                  do {
                    final Record object = JdbcQueryIterator.getNextObject(
                      recordStore, recordDefinition, attributes, recordFactory,
                      resultSet);
                    results.add(object);
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
      this.numResults = recordStore.getRowCount(query);
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
