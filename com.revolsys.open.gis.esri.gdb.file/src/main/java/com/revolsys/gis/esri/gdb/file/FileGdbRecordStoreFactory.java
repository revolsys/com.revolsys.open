package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreFactory;
import com.revolsys.data.io.RecordStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.util.CollectionUtil;

public class FileGdbRecordStoreFactory implements RecordStoreFactory {

  private static final List<String> FILE_NAME_EXTENSIONS = Arrays.asList("gdb");

  private static final List<String> URL_PATTERNS = Arrays.asList(
    "file:/(//)?.*.gdb/?", "folderconnection:/(//)?.*.gdb/?");

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<String, AtomicInteger>();

  private static final Map<String, CapiFileGdbRecordStore> DATA_STORES = new HashMap<String, CapiFileGdbRecordStore>();

  public static CapiFileGdbRecordStore create(File file) {
    if (file == null) {
      return null;
    } else {
      synchronized (COUNTS) {
        final String fileName = FileUtil.getCanonicalPath(file);
        file = new File(fileName);
        final AtomicInteger count = CollectionUtil.get(COUNTS, fileName,
          new AtomicInteger());
        count.incrementAndGet();
        CapiFileGdbRecordStore recordStore = DATA_STORES.get(fileName);
        if (recordStore == null) {
          recordStore = new CapiFileGdbRecordStore(file);
          recordStore.setCreateMissingRecordStore(false);
          DATA_STORES.put(fileName, recordStore);
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
        final AtomicInteger countHolder = CollectionUtil.get(COUNTS, fileName,
          new AtomicInteger());
        final int count = countHolder.decrementAndGet();
        if (count <= 0) {
          COUNTS.remove(fileName);
          final CapiFileGdbRecordStore recordStore = DATA_STORES.remove(fileName);
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
    final Map<String, Object> properties = new LinkedHashMap<String, Object>(
      connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    final FileGdbRecordStore recordStore = create(file);
    RecordStoreFactoryRegistry.setConnectionProperties(recordStore,
      properties);
    return recordStore;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getFileExtensions() {
    return FILE_NAME_EXTENSIONS;
  }

  @Override
  public String getName() {
    return "ESRI File Geodatabase";
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
