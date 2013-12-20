package com.revolsys.gis.spatialite.io;

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
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.util.JavaBeanUtil;

public class SpatiaLiteDatabaseFactory implements JdbcDatabaseFactory {
  public static final String URL_REGEX = "jdbc:sqlite:(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  @Override
  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  @Override
  public void closeDataSource(final DataSource dataSource) {
    // Not pooling
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(final DataSource dataSource) {
    return new SpatiaLiteDataObjectStore(dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new SpatiaLiteDataObjectStore(this, connectionProperties);
  }

  @Override
  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    try {
      final Map<String, Object> newConfig = new HashMap<String, Object>(config);
      final String url = (String)newConfig.remove("url");

      final SQLiteConfig sqliteConfig = new SQLiteConfig();
      sqliteConfig.enableLoadExtension(true);

      // final Connection connection = DriverManager.getConnection(url,
      // sqliteConfig.toProperties());
      // try {
      // final Statement statement = connection.createStatement();
      // try {
      // statement.execute("SELECT load_extension('/usr/local/lib/libspatialite.dylib')");
      // } finally {
      // statement.close();
      // }
      // } finally {
      // connection.close();
      // }
      final SQLiteDataSource dataSource = new SQLiteDataSource();
      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          JavaBeanUtil.setProperty(dataSource, name, value);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(SpatiaLiteDatabaseFactory.class).debug(
            "Unable to set SpatiaLite data source property " + name, e);
        }
      }
      dataSource.setUrl(url);

      return dataSource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to create data source for "
        + config, e);
    }
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  @Override
  public List<String> getFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public String getName() {
    return "SpatiaLite/SQLite Database";
  }

  @Override
  public List<String> getProductNames() {
    return Collections.singletonList("SQLite");
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }
}
