package com.revolsys.gis.postgresql;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.io.DataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PasswordUtil;

public class PostgreSQLDatabaseFactory implements JdbcDatabaseFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabaseFactory.class);

  private static final String URL_REGEX = "jdbc:postgresql:(?://([^:]+)(?::(\\d+))?/)?(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  @Override
  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  @Override
  public void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof PGPoolingDataSource) {
      final PGPoolingDataSource postgreSqlDataSource = (PGPoolingDataSource)dataSource;
      postgreSqlDataSource.close();
    }
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(final DataSource dataSource) {
    return new PostgreSQLDataObjectStore(dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new PostgreSQLDataObjectStore(this, connectionProperties);
  }

  @Override
  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    final Map<String, Object> newConfig = new HashMap<String, Object>(config);
    final String url = (String)newConfig.remove("url");
    final String username = (String)newConfig.remove("username");
    String password = (String)newConfig.remove("password");
    password = PasswordUtil.decrypt(password);
    final Matcher urlMatcher = URL_PATTERN.matcher(url);

    final boolean matches = urlMatcher.matches();
    if (matches) {
      final String serverName = urlMatcher.group(1);
      final String port = urlMatcher.group(2);
      final String databaseName = urlMatcher.group(3);

      final PGPoolingDataSource dataSource = new PGPoolingDataSource();
      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          JavaBeanUtil.setProperty(dataSource, name, value);
        } catch (final Throwable t) {
          LOG.debug("Unable to set data source property " + name + " = "
            + value + " for " + url, t);
        }
      }

      dataSource.setDatabaseName(databaseName);
      dataSource.setUser(username);
      dataSource.setPassword(password);
      if (serverName != null) {
        dataSource.setServerName(serverName);
      }
      if (port != null) {
        dataSource.setPortNumber(Integer.parseInt(port));
      }
      return dataSource;
    } else {
      throw new IllegalArgumentException("Not a valid postgres JDBC URL " + url);
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
    return "PostgreSQL/PostGIS Database";
  }

  @Override
  public List<String> getProductNames() {
    return Collections.singletonList("PostgreSQL");
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }
}
