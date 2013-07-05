package com.revolsys.gis.spatialite;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.util.JavaBeanUtil;

public class SpatiaLiteDatabaseFactory implements JdbcDatabaseFactory {
  private static final String URL_REGEX = "jdbc:spatialite:(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  @Override
  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  @Override
  public void closeDataSource(final DataSource dataSource) {
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(final DataSource dataSource) {
    return new SpatialLiteDataObjectStore(dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new SpatialLiteDataObjectStore(this, connectionProperties);
  }

  @Override
  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    final Map<String, Object> newConfig = new HashMap<String, Object>(config);
    final String url = (String)newConfig.remove("url");

    final SQLiteDataSource dataSource = new SQLiteDataSource();
    for (final Entry<String, Object> property : newConfig.entrySet()) {
      final String name = property.getKey();
      final Object value = property.getValue();
      try {
        JavaBeanUtil.setProperty(dataSource, name, value);
      } catch (final Throwable t) {
        LoggerFactory.getLogger(getClass()).debug(
          "Unable to set data source property " + name + " = " + value
            + " for " + url, t);
      }
    }

    dataSource.setUrl(url);

    return dataSource;
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  @Override
  public List<String> getProductNames() {
    return Collections.singletonList("SpatiaLite");
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }
}
