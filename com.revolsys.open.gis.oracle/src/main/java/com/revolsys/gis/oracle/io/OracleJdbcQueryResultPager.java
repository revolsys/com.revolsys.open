package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;

public class OracleJdbcQueryResultPager extends JdbcQueryResultPager {

  public OracleJdbcQueryResultPager(final JdbcDataObjectStore dataStore,
    final Map<String, Object> properties, final Query query) {
    super(dataStore, properties, query);
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    synchronized (this) {
      final JdbcDataObjectStore dataStore = getDataStore();
      final Query query = getQuery();
      setNumResults(dataStore.getRowCount(query));
      updateNumPages();

      final ArrayList<DataObject> results = new ArrayList<DataObject>();
      final int pageSize = getPageSize();
      final int pageNumber = getPageNumber();
      if (pageNumber != -1) {
        String sql = getSql();

        final int startRowNum = (pageNumber - 1) * pageSize + 1;
        final int endRowNum = startRowNum + pageSize - 1;
        sql = "SELECT * FROM ( SELECT  T2.*, ROWNUM TROWNUM FROM ( " + sql
          + ") T2 ) WHERE TROWNUM BETWEEN " + startRowNum + " AND " + endRowNum;

        final Connection connection = getConnection();
        try {
          final DataObjectFactory dataObjectFactory = getDataObjectFactory();
          final DataObjectMetaData metaData = getMetaData();
          final List<Attribute> attributes = new ArrayList<>();

          final List<String> attributeNames = query.getAttributeNames();
          if (attributeNames.isEmpty()) {
            attributes.addAll(metaData.getAttributes());
          } else {
            for (final String attributeName : attributeNames) {
              if (attributeName.equals("*")) {
                attributes.addAll(metaData.getAttributes());
              } else {
                final Attribute attribute = metaData.getAttribute(attributeName);
                if (attribute != null) {
                  attributes.add(attribute);
                }
              }
            }
          }
          try (
            final PreparedStatement statement = connection.prepareStatement(sql);
            final ResultSet resultSet = JdbcQueryIterator.getResultSet(
              metaData, statement, getQuery());) {
            if (resultSet.next()) {
              int i = 0;
              do {
                final DataObject object = JdbcQueryIterator.getNextObject(
                  dataStore, metaData, attributes, dataObjectFactory, resultSet);
                results.add(object);
                i++;
              } while (resultSet.next() && i < pageSize);
            }
          }
        } catch (final SQLException e) {
          JdbcUtils.getException(getDataSource(), connection, "updateResults",
            sql, e);
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).error("Error reading from pager",
            t);
        }
        setResults(results);
      }
    }
  }
}
