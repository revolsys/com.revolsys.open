package com.revolsys.gis.oracle.io;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.schema.RecordStore;

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

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

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

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new OracleRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    return new OracleRecordStore(this, connectionProperties);
  }
}
