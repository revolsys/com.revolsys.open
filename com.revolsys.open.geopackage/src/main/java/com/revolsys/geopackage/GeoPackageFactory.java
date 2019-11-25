package com.revolsys.geopackage;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.schema.RecordStore;

public class GeoPackageFactory implements FileRecordStoreFactory {

  public static final String DESCRIPTION = "GeoPackageFactory Database";

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<>();

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gpkg");

  private static final Map<String, GeoPackageRecordStore> RECORD_STORES = new HashMap<>();

  private static final List<Pattern> URL_PATTERNS = Arrays.asList(
    Pattern.compile("[^(file:)].*.gpkg/?"), Pattern.compile("file:(/(//)?)?.*.gpkg/?"),
    Pattern.compile("folderconnection:/(//)?.*.gpkg/?"));

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
          recordStore = new GeoPackageRecordStore(file);
          RECORD_STORES.put(fileName, recordStore);
        }
        return recordStore;
      }
    }
  }

  @Override
  public boolean canOpenPath(final Path path) {
    if (FileRecordStoreFactory.super.canOpenPath(path)) {
      try {
        // GPKG must be a file not inside a zip file
        path.toFile();
        return true;
      } catch (final UnsupportedOperationException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return DESCRIPTION;
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
  public boolean isDirectory() {
    return false;
  }

  @Override
  public GeoPackageRecordStore newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<>(connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    synchronized (COUNTS) {
      final GeoPackageRecordStore recordStore = newRecordStore(file);
      RecordStore.setConnectionProperties(recordStore, properties);
      return recordStore;
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}
