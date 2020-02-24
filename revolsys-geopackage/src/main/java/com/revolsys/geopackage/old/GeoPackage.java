package com.revolsys.geopackage.old;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.sqlite.SQLiteJDBCLoader;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.jdbc.io.AbstractJdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.RsCoreDataTypes;

/**
 * jdbc:sqlite:[file]
 */
public class GeoPackage extends AbstractJdbcDatabaseFactory implements FileRecordStoreFactory {
  private static final List<FieldDefinition> CONNECTION_FIELD_DEFINITIONS = Arrays.asList( //
    new FieldDefinition("file", RsCoreDataTypes.FILE, 50, true) //
  );

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<>();

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gpkg");

  public static final String JDBC_PREFIX = "jdbc:sqlite:";

  private static final Map<String, GeoPackageRecordStore> RECORD_STORES = new HashMap<>();

  private static final List<Pattern> URL_PATTERNS = Arrays.asList(Pattern.compile("jdbc:sqlite:.+"),
    Pattern.compile("[^(file:)].+\\.gpkg"), Pattern.compile("file:(/(//)?)?.+\\.gpkg"),
    Pattern.compile("folderconnection:/(//)?.*.gpkg"));

  static {
    try {
      SQLiteJDBCLoader.initialize();
    } catch (final Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static GeoPackageRecordStore newRecordStore(final File file) {
    if (file == null) {
      return null;
    } else {
      synchronized (COUNTS) {
        final String fileName = FileUtil.getCanonicalPath(file);
        final AtomicInteger count = Maps.get(COUNTS, fileName, new AtomicInteger());
        count.incrementAndGet();
        GeoPackageRecordStore recordStore = RECORD_STORES.get(fileName);
        if (recordStore == null || recordStore.isClosed()) {
          final MapEx properties = new LinkedHashMapEx().add("url", "jdbc:sqlite:" + fileName);
          recordStore = new GeoPackage().newRecordStore(properties);
          RECORD_STORES.put(fileName, recordStore);
        }
        return recordStore;
      }
    }
  }

  public static GeoPackageRecordStore newRecordStore(final Path path) {
    if (path == null) {
      return null;
    } else {
      return newRecordStore(path.toFile());
    }
  }

  @Override
  public boolean canOpenPath(final Path path) {
    if (isAvailable()) {
      final String fileNameExtension = Paths.getFileNameExtension(path);
      return getRecordStoreFileExtensions().contains(fileNameExtension);
    } else {
      return false;
    }
  }

  @Override
  public boolean canOpenUrl(final String url) {
    if (isAvailable()) {
      for (final Pattern pattern : getUrlPatterns()) {
        if (pattern.matcher(url).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<FieldDefinition> getConnectionFieldDefinitions() {
    return CONNECTION_FIELD_DEFINITIONS;
  }

  @Override
  public String getConnectionValidationQuery() {
    return null;
  }

  @Override
  public String getDriverClassName() {
    return "org.sqlite.JDBC";
  }

  @Override
  public String getName() {
    return "GeoPackageFactory Database";
  }

  @Override
  public String getProductName() {
    return "GeoPackageFactory";
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return FILE_NAME_EXTENSIONS;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public List<Pattern> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public String getVendorName() {
    return "sqlite";
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public DataSource newDataSource(final Map<String, ? extends Object> config) {
    final MapEx newConfig = new LinkedHashMapEx(config);
    final String url = newConfig.getString("url");
    if (Property.hasValue(url) && !url.startsWith("jdbc")) {
      try {
        final UrlResource resource = new UrlResource(url);
        final File file = resource.getFile();
        final String newUrl = JDBC_PREFIX + FileUtil.getCanonicalPath(file);
        newConfig.put("url", newUrl);
      } catch (final Exception e) {
        throw new IllegalArgumentException(url + " must be a file", e);
      }
    }
    newConfig.put("enable_load_extension", true);
    return super.newDataSource(newConfig);
  }

  @Override
  public GeoPackageRecordStore newRecordStore(final DataSource dataSource) {
    return new GeoPackageRecordStore(dataSource);
  }

  @Override
  public GeoPackageRecordStore newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new GeoPackageRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    if (url != null && url.startsWith(JDBC_PREFIX)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      final String fileName = url.substring(JDBC_PREFIX.length());
      parameters.put("recordStoreType", getName());
      parameters.put("file", fileName);
    }
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final StringBuilder url = new StringBuilder(JDBC_PREFIX);
    final String file = Maps.getString(urlParameters, "file");
    url.append(file);
    return url.toString().toLowerCase();
  }
}
