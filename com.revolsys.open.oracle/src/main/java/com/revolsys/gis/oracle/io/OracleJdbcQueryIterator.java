package com.revolsys.gis.oracle.io;

import java.util.Map;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.record.query.Query;

public class OracleJdbcQueryIterator extends JdbcQueryIterator {

  public OracleJdbcQueryIterator(final AbstractJdbcRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    super(recordStore, query, properties);
  }

  @Override
  protected String getSql(final Query query) {
    String sql = super.getSql(query);

    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset < 1 && limit == Integer.MAX_VALUE) {
      return sql;
    }
    sql = "SELECT * FROM (SELECT V.*,ROWNUM \"ROWN\" FROM (" + sql + ") V ) WHERE ROWN ";
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
