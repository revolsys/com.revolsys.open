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
import org.springframework.beans.DirectFieldAccessor;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;

public class PostgreSQLDatabaseFactory implements JdbcDatabaseFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabaseFactory.class);

  private static final String URL_REGEX = "jdbc:postgresql:(?://([^:]+)(?::(\\d+))?/)?(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  public void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof PGPoolingDataSource) {
      final PGPoolingDataSource postgreSqlDataSource = (PGPoolingDataSource)dataSource;
      postgreSqlDataSource.close();
    }
  }

  public JdbcDataObjectStore createDataObjectStore(final DataSource dataSource) {
    return new PostgreSQLDataObjectStore(dataSource);
  }

  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new PostgreSQLDataObjectStore(this, connectionProperties);
  }

  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    final Map<String, Object> newConfig = new HashMap<String, Object>(config);
    final String url = (String)newConfig.remove("url");
    final String username = (String)newConfig.remove("username");
    final String password = (String)newConfig.remove("password");
    final Matcher urlMatcher = URL_PATTERN.matcher(url);

    final boolean matches = urlMatcher.matches();
    if (matches) {
      final String serverName = urlMatcher.group(1);
      final String port = urlMatcher.group(2);
      final String databaseName = urlMatcher.group(3);

      final PGPoolingDataSource dataSource = new PGPoolingDataSource();
      final DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(
        dataSource);
      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          dataSourceBean.setPropertyValue(name, value);
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

  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  public List<String> getProductNames() {
    return Collections.singletonList("PostgreSQL");
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }
}
