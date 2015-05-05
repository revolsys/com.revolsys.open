package com.revolsys.gis.oracle.io;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import oracle.jdbc.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;
import oracle.net.jdbc.nl.NLParamParser;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PasswordUtil;

/**
jdbc:oracle:thin:@//<host>:<port>/<ServiceName>
jdbc:oracle:thin:@<host>:<port>:<SID>
jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=<host>)(PORT=<port>))(CONNECT_DATA=(SERVICE_NAME=<service>)))
jdbc:oracle:oci:@<tnsname>
jdbc:oracle:oci:@<host>:<port>:<sid>
jdbc:oracle:oci:@<host>:<port>/<service>
 */
public class OracleDatabaseFactory extends AbstractJdbcDatabaseFactory {
  public static final String URL_REGEX = "jdbc:oracle:thin:(.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public static List<String> getTnsConnectionNames() {
    File tnsFile = new File(System.getProperty("oracle.net.tns_admin"), "tnsnames.ora");
    if (!tnsFile.exists()) {
      tnsFile = new File(System.getenv("TNS_ADMIN"), "tnsnames.ora");
      if (!tnsFile.exists()) {
        tnsFile = new File(System.getenv("ORACLE_HOME") + "/network/admin", "tnsnames.ora");
        if (!tnsFile.exists()) {
          tnsFile = new File(System.getenv("ORACLE_HOME") + "/NETWORK/ADMIN", "tnsnames.ora");

        }
      }
    }
    if (tnsFile.exists()) {
      try {
        final NLParamParser parser = new NLParamParser(new FileReader(tnsFile));
        return Arrays.asList(parser.getNLPAllNames());
      } catch (final Throwable e) {
        LoggerFactory.getLogger(OracleDatabaseFactory.class).error("Error reading: " + tnsFile, e);
      }
    }
    return Collections.emptyList();
  }

  protected void addCacheProperty(final Map<String, Object> config, final String key,
    final Properties cacheProperties, final String propertyName, final Object defaultValue,
    final DataType dataType) {
    Object value = config.remove(key);
    if (value == null) {
      value = config.get(propertyName);
    }
    cacheProperties.put(propertyName, String.valueOf(defaultValue));
    if (value != null) {
      try {
        final Object propertyValue = StringConverterRegistry.toObject(dataType, value);
        final String stringValue = String.valueOf(propertyValue);
        cacheProperties.put(propertyName, stringValue);
      } catch (final Throwable e) {
      }
    }
  }

  @Override
  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  @Override
  public void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof OracleDataSource) {
      final OracleDataSource oracleDataSource = (OracleDataSource)dataSource;
      try {
        oracleDataSource.close();
      } catch (final SQLException e) {
        LoggerFactory.getLogger(OracleDatabaseFactory.class).warn("Unable to close data source", e);
      }
    }
    super.closeDataSource(dataSource);
  }

  @Override
  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    if (isUseCommonsDbcp()) {
      return super.createDataSource(config);
    } else {
      try {
        final Map<String, Object> newConfig = new HashMap<String, Object>(config);
        final Properties cacheProperties = new Properties();
        final String url = (String)newConfig.remove("url");
        final String username = (String)newConfig.remove("username");
        String password = (String)newConfig.remove("password");
        password = PasswordUtil.decrypt(password);
        addCacheProperty(newConfig, "minPoolSize", cacheProperties, "MinLimit", 0, DataTypes.INT);
        addCacheProperty(newConfig, "maxPoolSize", cacheProperties, "MaxLimit", 10, DataTypes.INT);
        addCacheProperty(newConfig, "inactivityTimeout", cacheProperties, "InactivityTimeout", 300,
          DataTypes.INT);
        addCacheProperty(newConfig, "waitTimeout", cacheProperties, "ConnectionWaitTimeout", 10,
          DataTypes.INT);
        addCacheProperty(newConfig, "validateConnection", cacheProperties, "ValidateConnection",
          Boolean.TRUE, DataTypes.BOOLEAN);

        final OracleDataSource dataSource = new OracleDataSource();

        dataSource.setConnectionCachingEnabled(true);
        dataSource.setConnectionCacheProperties(cacheProperties);

        for (final Entry<String, Object> property : newConfig.entrySet()) {
          final String name = property.getKey();
          final Object value = property.getValue();
          try {
            JavaBeanUtil.setProperty(dataSource, name, value);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(OracleDatabaseFactory.class).debug(
              "Unable to set Oracle data source property " + name, e);
          }
        }
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Unable to create data source for " + config, e);
      }
    }
  }

  @Override
  public JdbcRecordStore createRecordStore(final DataSource dataSource) {
    return new OracleRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore createRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new OracleRecordStore(this, connectionProperties);
  }

  @Override
  public String getConnectionValidationQuery() {
    return "SELECT 1 FROM DUAL";
  }

  @Override
  public String getDriverClassName() {
    return OracleDriver.class.getName();
  }

  @Override
  public String getName() {
    return "Oracle Database";
  }

  @Override
  public List<String> getProductNames() {
    return Collections.singletonList("Oracle");
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
}
