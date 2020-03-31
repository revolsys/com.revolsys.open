package com.revolsys.oracle.recordstore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;

public class OracleJdbcQueryResultPager extends JdbcQueryResultPager {

  public OracleJdbcQueryResultPager(final JdbcRecordStore recordStore,
    final Map<String, Object> properties, final Query query) {
    super(recordStore, properties, query);
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    synchronized (this) {
      final JdbcRecordStore recordStore = getRecordStore();
      final Query query = getQuery();
      setNumResults(recordStore.getRecordCount(query));
      updateNumPages();

      final ArrayList<Record> results = new ArrayList<>();
      final int pageSize = getPageSize();
      final int pageNumber = getPageNumber();
      if (pageNumber != -1) {
        String sql = getSql();

        final int startRowNum = (pageNumber - 1) * pageSize + 1;
        final int endRowNum = startRowNum + pageSize - 1;
        sql = "SELECT * FROM ( SELECT  T2.*, ROWNUM TROWNUM FROM ( " + sql
          + ") T2 ) WHERE TROWNUM BETWEEN " + startRowNum + " AND " + endRowNum;

        try (
          final JdbcConnection connection = getRecordStore().getJdbcConnection()) {
          final RecordFactory<Record> recordFactory = getRecordFactory();
          final RecordDefinition recordDefinition = getRecordDefinition();

          try (
            final PreparedStatement statement = connection.prepareStatement(sql);
            final ResultSet resultSet = recordStore.getResultSet(statement, getQuery());) {
            if (resultSet.next()) {
              int i = 0;
              do {
                final Record record = JdbcQueryIterator.getNextRecord(recordStore, recordDefinition,
                  this.fields, recordFactory, resultSet, this.internStrings);
                results.add(record);
                i++;
              } while (resultSet.next() && i < pageSize);
            }
          } catch (final SQLException e) {
            throw connection.getException("updateResults", sql, e);
          }
        }
        setResults(results);
      }
    }
  }
}
