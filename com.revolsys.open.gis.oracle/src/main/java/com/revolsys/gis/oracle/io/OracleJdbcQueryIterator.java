package com.revolsys.gis.oracle.io;

import java.util.Map;

import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;

public class OracleJdbcQueryIterator extends JdbcQueryIterator {

  public OracleJdbcQueryIterator(final AbstractJdbcDataObjectStore dataStore,
    final Query query, final Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(Query query) {
    final OracleDataObjectStore dataStore = (OracleDataObjectStore)getDataStore();
    query = dataStore.addBoundingBoxFilter(query);
    setQuery(query);
    String sql = super.getSql(query);

    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset < 1 && limit < 0) {
      return sql;
    }
    sql = "SELECT * FROM (SELECT V.*,ROWNUM \"ROWN\" FROM (" + sql
      + ") V ) WHERE ROWN ";
    final int startRowNum = offset + 1;
    final int endRowNum = offset + limit;
    if (offset > 0) {
      if (limit < 0) {
        return sql + " >= " + startRowNum;
      } else {
        return sql + " BETWEEN " + startRowNum + " AND " + endRowNum;
      }
    } else {
      return sql + " <= " + endRowNum;
    }
  }

}
