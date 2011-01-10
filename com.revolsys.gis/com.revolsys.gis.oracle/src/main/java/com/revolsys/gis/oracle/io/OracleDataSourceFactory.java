package com.revolsys.gis.oracle.io;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import com.revolsys.gis.jdbc.io.DataSourceFactory;

public class OracleDataSourceFactory implements DataSourceFactory {
  public DataSource createDataSource(Map<String, Object> config)
    throws SQLException {
    String url = (String)config.get("url");
    String username = (String)config.get("username");
    String password = (String)config.get("password");
    OracleDataSource dataSource = new OracleDataSource();
    dataSource.setURL(url);
    dataSource.setUser(username);
    dataSource.setPassword(password);
    dataSource.setConnectionCachingEnabled(true);
    Properties properties = new Properties();
    properties.putAll(config);
    dataSource.setConnectionCacheProperties(properties);
    dataSource.setConnectionProperties(properties);
    return dataSource;
  }
}
