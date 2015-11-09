package com.revolsys.gis.postgresql;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.postgresql.Driver;

import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.UrlUtil;

public class PostgreSQL implements JdbcDatabaseFactory {
  @Override
  public String getDriverClassName() {
    return Driver.class.getName();
  }

  @Override
  public String getName() {
    return "PostgreSQL/PostGIS Database";
  }

  @Override
  public String getProductName() {
    return "PostgreSQL";
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public String getVendorName() {
    return "postgresql";
  }

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new PostgreSQLRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new PostgreSQLRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseJdbcUrl(final String url) {
    if (url != null && url.startsWith("jdbc:postgresql")) {
      final Matcher hostMatcher = Pattern
        .compile(
          "jdbc:postgresql:(?://([^:]+)(?::(\\d+))?/)?([^:?]+)(?:\\?(\\w+=.*(?:&\\w+=.*)*))?")
        .matcher(url);
      final Map<String, Object> parameters = new LinkedHashMap<>();
      if (hostMatcher.matches()) {
        parameters.put("recordStoreType", getProductName());
        final Map<String, Object> urlParameters = UrlUtil.getQueryStringMap(hostMatcher.group(4));
        parameters.putAll(urlParameters);

        final String host = hostMatcher.group(1);
        parameters.put("host", host);
        final String port = hostMatcher.group(2);
        parameters.put("port", port);
        final String database = hostMatcher.group(3);
        parameters.put("database", database);
        parameters.put("namedConnection", null);
        return parameters;
      }
    }
    return Collections.emptyMap();
  }
}
