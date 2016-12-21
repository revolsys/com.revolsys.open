package com.revolsys.geopackage;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.sqlite.SQLiteJDBCLoader;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;

/**
 * jdbc:sqlite:[file]
 */
public class GeoPackage implements JdbcDatabaseFactory, FileRecordStoreFactory {
  static {
    try {
      SQLiteJDBCLoader.initialize();
    } catch (final Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gpkg");

  private static final List<Pattern> URL_PATTERNS = Arrays.asList(Pattern.compile("jdbc:sqlite:.+"),
    Pattern.compile("[^(file:)].+\\.gpkg"), Pattern.compile("file:(/(//)?)?.+\\.gpkg"),
    Pattern.compile("folderconnection:/(//)?.*.gpkg"));

  public static final String JDBC_PREFIX = "jdbc:sqlite:";

  private static final List<FieldDefinition> CONNECTION_FIELD_DEFINITIONS = Arrays.asList( //
    new FieldDefinition("file", DataTypes.FILE, 50, true) //
  );

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
    return "GeoPackage Database";
  }

  @Override
  public String getProductName() {
    return "GeoPackage";
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
    return JdbcDatabaseFactory.super.newDataSource(newConfig);
  }

  @Override
  public JdbcRecordStore newRecordStore(final DataSource dataSource) {
    return new GeoPackageRecordStore(dataSource);
  }

  @Override
  public JdbcRecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
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
