package com.revolsys.gis.postgresql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.postgresql.Driver;

import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.schema.RecordStore;

public class PostgreSQLDatabaseFactory extends AbstractJdbcDatabaseFactory {
  private static final String URL_REGEX = "jdbc:postgresql:(?://([^:]+)(?::(\\d+))?/)?(.+)";

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  @Override
  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  @Override
  public String getDriverClassName() {
    return Driver.class.getName();
  }

  @Override
  public String getName() {
    return "PostgreSQL/PostGIS Database";
  }

  @Override
  public List<String> getProductNames() {
    return Collections.singletonList("PostgreSQL");
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
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new PostgreSQLRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new PostgreSQLRecordStore(this, connectionProperties);
  }
}
