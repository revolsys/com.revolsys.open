package com.revolsys.oracle.recordstore;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.datatype.DataType;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

/**
jdbc:oracle:thin:@//<host>:<port>/<ServiceName>
jdbc:oracle:thin:@<host>:<port>:<SID>
jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=<host>)(PORT=<port>))(CONNECT_DATA=(SERVICE_NAME=<service>)))
jdbc:oracle:oci:@<tnsname>
jdbc:oracle:oci:@<host>:<port>:<sid>
jdbc:oracle:oci:@<host>:<port>/<service>
 */
public class Oracle implements JdbcDatabaseFactory {
  public static List<String> getTnsConnectionNames() {
    File tnsFile = new File(System.getProperty("oracle.net.tns_admin"), "tnsnames.ora");
    if (!tnsFile.exists()) {
      final String tnsAdmin = System.getenv("TNS_ADMIN");
      if (tnsAdmin != null) {
        tnsFile = new File(tnsAdmin, "tnsnames.ora");
      }
      if (!tnsFile.exists()) {
        final String oracleHome = System.getenv("ORACLE_HOME");
        if (oracleHome != null) {
          tnsFile = new File(oracleHome + "/network/admin", "tnsnames.ora");
        }
        if (!tnsFile.exists()) {
          if (oracleHome != null) {
            tnsFile = new File(oracleHome + "/NETWORK/ADMIN", "tnsnames.ora");
          }
        }
      }
    }
    if (tnsFile.exists()) {
      try {
        final FileReader reader = new FileReader(tnsFile);
        final Class<?> parserClass = Class.forName("oracle.net.jdbc.nl.NLParamParser");
        final Constructor<?> constructor = parserClass.getConstructor(Reader.class);
        final Object parser = constructor.newInstance(reader);
        final Method method = parserClass.getMethod("getNLPAllNames");
        final String[] names = (String[])method.invoke(parser);
        return Arrays.asList(names);
      } catch (final NoSuchMethodException e) {
      } catch (final ClassNotFoundException e) {
      } catch (final Throwable e) {
        LoggerFactory.getLogger(Oracle.class).error("Error reading: " + tnsFile, e);
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
        final Object propertyValue = StringConverter.toObject(dataType, value);
        final String stringValue = String.valueOf(propertyValue);
        cacheProperties.put(propertyName, stringValue);
      } catch (final Throwable e) {
      }
    }
  }

  @Override
  public Map<String, String> getConnectionUrlMap() {
    final Map<String, String> connectionMap = new TreeMap<>();
    for (final String connectionName : getTnsConnectionNames()) {
      final String connectionUrl = "jdbc:oracle:thin:@" + connectionName;
      connectionMap.put(connectionName, connectionUrl);
    }
    return connectionMap;
  }

  @Override
  public String getConnectionValidationQuery() {
    return "SELECT 1 FROM DUAL";
  }

  @Override
  public String getDriverClassName() {
    return "oracle.jdbc.OracleDriver";
  }

  @Override
  public String getName() {
    return "Oracle Database";
  }

  @Override
  public String getProductName() {
    return "Oracle";
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public String getVendorName() {
    return "oracle";
  }

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new OracleRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new OracleRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseJdbcUrl(final String url) {
    if (url != null && url.startsWith("jdbc:oracle")) {
      final Matcher hostMatcher = Pattern
        .compile("jdbc:oracle:(?:thin|oci):([^/@]+)?(?:/([^@]+))?@/?([^:]+)(?::(\\d+))?[/:]([^:]+)")
        .matcher(url);
      final Map<String, Object> parameters = new LinkedHashMap<>();
      if (hostMatcher.matches()) {
        parameters.put("recordStoreType", getProductName());
        final String user = hostMatcher.group(1);
        if (Property.hasValue(user)) {
          parameters.put("user", user);
        }
        final String password = hostMatcher.group(2);
        if (Property.hasValue(password)) {
          parameters.put("password", password);
        }
        final String host = hostMatcher.group(3);
        parameters.put("host", host);
        final String port = hostMatcher.group(4);
        parameters.put("port", port);
        final String database = hostMatcher.group(5);
        parameters.put("database", database);
        parameters.put("namedConnection", null);
        return parameters;
      }
      final Matcher tnsmatcher = Pattern
        .compile("jdbc:oracle:(?:thin|oci):([^/@]+)?(?:/([^@]+))?@([^:/]+)").matcher(url);
      if (tnsmatcher.matches()) {
        parameters.put("databaseType", getProductName());
        final String user = tnsmatcher.group(1);
        if (Property.hasValue(user)) {
          parameters.put("user", user);
        }
        final String password = tnsmatcher.group(2);
        if (Property.hasValue(password)) {
          parameters.put("password", password);
        }
        parameters.put("host", null);
        parameters.put("port", null);
        parameters.put("database", null);
        final String tnsname = tnsmatcher.group(3);
        parameters.put("namedConnection", tnsname);
        return parameters;
      }
    }
    return Collections.emptyMap();
  }
}
