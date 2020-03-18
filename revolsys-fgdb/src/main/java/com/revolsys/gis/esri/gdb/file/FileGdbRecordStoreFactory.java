package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.net.URI;
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
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.file.Paths;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.io.RecordStoreRecordAndGeometryWriterFactory;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class FileGdbRecordStoreFactory implements FileRecordStoreFactory {
  public static final String DESCRIPTION = "ESRI File Geodatabase";

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<>();

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gdb");

  private static final Map<String, FileGdbRecordStore> RECORD_STORES = new HashMap<>();

  private static final List<Pattern> URL_PATTERNS = Arrays.asList(
    Pattern.compile("[^(file:)].*.gdb/?"), Pattern.compile("file:(/(//)?)?.*.gdb/?"),
    Pattern.compile("folderconnection:/(//)?.*.gdb/?"));

  static {
    final RecordStoreRecordAndGeometryWriterFactory writerFactory = new RecordStoreRecordAndGeometryWriterFactory(
      DESCRIPTION, "application/x-esri-gdb", true, true, "gdb");
    IoFactoryRegistry.addFactory(writerFactory);
  }

  public static FileGdbRecordStore newRecordStore(final File file) {
    if (file == null) {
      return null;
    } else {
      synchronized (COUNTS) {
        final String fileName = FileUtil.getCanonicalPath(file);
        final AtomicInteger count = Maps.get(COUNTS, fileName, new AtomicInteger());
        count.incrementAndGet();
        FileGdbRecordStore recordStore = RECORD_STORES.get(fileName);
        if (recordStore == null || recordStore.isClosed()) {
          recordStore = new FileGdbRecordStore(file);
          RECORD_STORES.put(fileName, recordStore);
        }
        return recordStore;
      }
    }
  }

  public static FileGdbRecordStore newRecordStore(final Path path) {
    if (path == null) {
      return null;
    } else {
      final File file = path.toFile();
      return newRecordStore(file);
    }
  }

  public static FileGdbRecordStore newRecordStoreInitialized(final File file) {
    final FileGdbRecordStore recordStore = newRecordStore(file);
    recordStore.initialize();
    return recordStore;
  }

  public static FileGdbRecordStore newRecordStoreInitialized(final Path path) {
    final FileGdbRecordStore recordStore = newRecordStore(path);
    recordStore.initialize();
    return recordStore;
  }

  /**
   * Release the record store for the file. Decrements the count of references to the file. If
   * the count <=0 then the record store will be removed.
   *
   * @param fileName
   * @return True if the record store has no references and was released. False otherwise
   */
  static boolean release(final FileGdbRecordStore recordStore) {
    synchronized (COUNTS) {
      final String fileName = recordStore.getFileName();
      final FileGdbRecordStore currentRecordStore = RECORD_STORES.get(fileName);
      if (currentRecordStore == recordStore) {
        final AtomicInteger countHolder = Maps.get(COUNTS, fileName, new AtomicInteger());
        final int count = countHolder.decrementAndGet();
        if (count <= 0) {
          COUNTS.remove(fileName);
          RECORD_STORES.remove(fileName);
          return true;
        }
      } else {
        return !recordStore.isClosed();
      }
    }
    return false;
  }

  @Override
  public boolean canOpenPath(final Path path) {
    if (FileRecordStoreFactory.super.canOpenPath(path)) {
      try {
        // FGDB must be a file not inside a zip file
        path.toFile();
      } catch (final UnsupportedOperationException e) {
        return false;
      }
      if (Paths.exists(Paths.getPath(path, "timestamps"))) {
        return true;
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
    return RecordStore.class;
  }

  @Override
  public List<Pattern> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public boolean isReadFromDirectorySupported() {
    return true;
  }

  @Override
  public FileGdbRecordStore newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<>(connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    synchronized (COUNTS) {
      final FileGdbRecordStore recordStore = newRecordStore(file);
      RecordStore.setConnectionProperties(recordStore, properties);
      return recordStore;
    }
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    try {
      final URI uri = UrlUtil.getUri(url);
      final File file = FileUtil.getFile(uri);
      if (file != null) {
        parameters.put("recordStoreType", getName());
        parameters.put("file", file);
      }
    } catch (final Throwable e) {
    }
    return parameters;
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final String file = Maps.getString(urlParameters, "file");
    if (Property.hasValue(file)) {
      try {
        return FileUtil.toUrlString(file);
      } catch (final Throwable e) {
        return null;
      }
    }
    return null;
  }
}
