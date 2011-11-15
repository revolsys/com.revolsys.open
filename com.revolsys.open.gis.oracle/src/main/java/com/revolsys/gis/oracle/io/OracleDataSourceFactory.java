package com.revolsys.gis.oracle.io;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;

import com.revolsys.jdbc.io.DataSourceFactory;

public class OracleDataSourceFactory implements DataSourceFactory {
  public static final List<String> URL_PATTERNS = Arrays.asList("jdbc:oracle:thin:(?:([^/]+)(?:/([^@]+))?)?@(?://)?([^:]+)(?::([^:]+))[:/](.+)");

  private static final Logger LOG = LoggerFactory.getLogger(OracleDataSourceFactory.class);

  public DataSource createDataSource(Map<String, Object> config)
    throws SQLException {
    Map<String, Object> newConfig = new HashMap<String, Object>(config);
    String url = (String)newConfig.remove("url");
    String username = (String)newConfig.remove("username");
    String password = (String)newConfig.remove("password");
    OracleDataSource dataSource = new OracleDataSource();

    dataSource.setConnectionCachingEnabled(true);
    DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(dataSource);
    for (Entry<String, Object> property : newConfig.entrySet()) {
      String name = property.getKey();
      Object value = property.getValue();
      try {
        dataSourceBean.setPropertyValue(name, value);
      } catch (Throwable e) {
        LOG.error("Unable to set Oracle data source property " + name, e);
      }
    }
    dataSource.setURL(url);
    dataSource.setUser(username);
    dataSource.setPassword(password);

    return dataSource;
  }

  public void closeDataSource(DataSource dataSource) {
    if (dataSource instanceof OracleDataSource) {
      OracleDataSource oracleDataSource = (OracleDataSource)dataSource;
      try {
        oracleDataSource.close();
      } catch (SQLException e) {
        LOG.warn("Unable to close data source", e);
      }
    }
  }
}
