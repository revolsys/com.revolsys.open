package com.revolsys.gis.oracle.io;

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
      setNumResults(recordStore.getRowCount(query));
      updateNumPages();

      final ArrayList<Record> results = new ArrayList<Record>();
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
          final RecordFactory recordFactory = getRecordFactory();
          final RecordDefinition recordDefinition = getRecordDefinition();
          final List<FieldDefinition> attributes = new ArrayList<>();

          final List<String> fieldNames = query.getFieldNames();
          if (fieldNames.isEmpty()) {
            attributes.addAll(recordDefinition.getFields());
          } else {
            for (final String fieldName : fieldNames) {
              if (fieldName.equals("*")) {
                attributes.addAll(recordDefinition.getFields());
              } else {
                final FieldDefinition attribute = recordDefinition.getField(fieldName);
                if (attribute != null) {
                  attributes.add(attribute);
                }
              }
            }
          }
          try (
            final PreparedStatement statement = connection.prepareStatement(sql);
            final ResultSet resultSet = JdbcQueryIterator.getResultSet(recordDefinition, statement,
              getQuery());) {
            if (resultSet.next()) {
              int i = 0;
              do {
                final Record object = JdbcQueryIterator.getNextRecord(recordStore, recordDefinition,
                  attributes, recordFactory, resultSet);
                results.add(object);
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
