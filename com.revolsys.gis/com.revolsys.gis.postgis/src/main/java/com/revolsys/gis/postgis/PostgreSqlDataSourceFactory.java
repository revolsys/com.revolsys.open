package com.revolsys.gis.postgis;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.DirectFieldAccessor;

import com.revolsys.gis.jdbc.io.DataSourceFactory;

public class PostgreSqlDataSourceFactory implements DataSourceFactory {
  private static final String URL_REGEX = "jdbc:postgresql:(?://([^:]+)(?::(\\d+))?/)?(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public DataSource createDataSource(Map<String, Object> config)
    throws SQLException {
    Map<String, Object> newConfig = new HashMap<String, Object>(config);
    String url = (String)newConfig.remove("url");
    String username = (String)newConfig.remove("username");
    String password = (String)newConfig.remove("password");
    Matcher urlMatcher = URL_PATTERN.matcher(url);

    if (urlMatcher.matches()) {
      String serverName = urlMatcher.group(1);
      String port = urlMatcher.group(2);
      String databaseName = urlMatcher.group(3);

      PGPoolingDataSource dataSource = new PGPoolingDataSource();
      DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(dataSource);
      for (Entry<String, Object> property : newConfig.entrySet()) {
        String name = property.getKey();
        Object value = property.getValue();
        dataSourceBean.setPropertyValue(name, value);
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
      throw new IllegalArgumentException("");
    }
  }

  public void closeDataSource(DataSource dataSource) {
    if (dataSource instanceof PGPoolingDataSource) {
      PGPoolingDataSource postgreSqlDataSource = (PGPoolingDataSource)dataSource;
      postgreSqlDataSource.close();
    }
  }
}
