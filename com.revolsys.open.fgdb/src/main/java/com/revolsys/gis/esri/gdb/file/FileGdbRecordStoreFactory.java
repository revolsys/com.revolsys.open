package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.collection.map.Maps;
import com.revolsys.data.record.io.RecordStoreFactory;
import com.revolsys.data.record.io.RecordStoreFactoryRegistry;
import com.revolsys.data.record.io.RecordStoreRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;

public class FileGdbRecordStoreFactory implements RecordStoreFactory {

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<String, AtomicInteger>();

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gdb");

  private static final Map<String, FileGdbRecordStore> RECORD_STORES = new HashMap<String, FileGdbRecordStore>();

  private static final List<String> URL_PATTERNS = Arrays.asList("file:/(//)?.*.gdb/?",
    "folderconnection:/(//)?.*.gdb/?");

  static {
    final RecordStoreRecordAndGeometryWriterFactory writerFactory = new RecordStoreRecordAndGeometryWriterFactory(
      "ESRI File Geodatabase", "application/x-esri-gdb", true, true, "gdb");
    IoFactoryRegistry.getInstance().addFactory(writerFactory);
  }

  public static FileGdbRecordStore create(final File file) {
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

  static boolean release(String fileName) {
    if (fileName == null) {
      return false;
    } else {
      synchronized (COUNTS) {
        fileName = FileUtil.getCanonicalPath(fileName);
        final AtomicInteger countHolder = Maps.get(COUNTS, fileName, new AtomicInteger());
        final int count = countHolder.decrementAndGet();
        if (count <= 0) {
          COUNTS.remove(fileName);
          final FileGdbRecordStore recordStore = RECORD_STORES.remove(fileName);
          if (recordStore == null) {
            return false;
          } else {
            recordStore.doClose();
          }
          COUNTS.remove(fileName);
          return true;
        } else {
          return true;
        }
      }
    }
  }

  @Override
  public FileGdbRecordStore createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>(connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    final FileGdbRecordStore recordStore = create(file);
    RecordStoreFactoryRegistry.setConnectionProperties(recordStore, properties);
    return recordStore;
  }

  @Override
  public String getName() {
    return "ESRI File Geodatabase";
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
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

}
