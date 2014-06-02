package com.revolsys.gis.postgresql;

import java.util.Map;

import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;

public class PostgreSQLJdbcQueryIterator extends JdbcQueryIterator {

  public PostgreSQLJdbcQueryIterator(final JdbcDataObjectStore dataStore,
    final Query query, final Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(final Query query) {
    String sql = super.getSql(query);
    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset > 0) {
      sql += " OFFSET " + offset;
    }
    if (limit > -1) {
      sql += " LIMIT " + limit;
    }
    return sql;
  }

}
