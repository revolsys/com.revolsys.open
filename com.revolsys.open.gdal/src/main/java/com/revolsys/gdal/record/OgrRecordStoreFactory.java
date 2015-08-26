package com.revolsys.gdal.record;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.gdal.ogr.ogr;

import com.revolsys.collection.map.Maps;
import com.revolsys.data.record.io.RecordStoreFactory;
import com.revolsys.data.record.io.RecordStoreFactoryRegistry;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gdal.Gdal;
import com.revolsys.io.FileUtil;

public class OgrRecordStoreFactory implements RecordStoreFactory {

  private static final Map<String, AtomicInteger> COUNTS = new HashMap<>();

  private static final Map<String, OgrRecordStore> DATA_STORES = new HashMap<>();

  private static OgrRecordStore create(final String driverName, File file) {
    if (file == null) {
      return null;
    } else {
      synchronized (COUNTS) {
        final String fileName = FileUtil.getCanonicalPath(file);
        file = new File(fileName);
        final AtomicInteger count = Maps.get(COUNTS, fileName, new AtomicInteger());
        count.incrementAndGet();
        OgrRecordStore recordStore = DATA_STORES.get(fileName);
        if (recordStore == null) {
          recordStore = new OgrRecordStore(driverName, file);
          // recordStore.setCreateMissingRecordStore(false);
          DATA_STORES.put(fileName, recordStore);
        }
        return recordStore;
      }
    }
  }

  static boolean release(final File file) {
    if (file == null) {
      return false;
    } else {
      synchronized (COUNTS) {
        final String fileName = FileUtil.getCanonicalPath(file);
        final AtomicInteger countHolder = Maps.get(COUNTS, fileName, new AtomicInteger());
        final int count = countHolder.decrementAndGet();
        if (count <= 0) {
          COUNTS.remove(fileName);
          final OgrRecordStore recordStore = DATA_STORES.remove(fileName);
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

  private boolean available;

  private final String driverName;

  private final List<String> fileNameExtensions;

  private final String name;

  private final List<String> urlPatterns = new ArrayList<>();

  public OgrRecordStoreFactory(final String name, final String driverName, final String mediaType,
    final List<String> fileNameExtensions) {
    this.name = name;
    this.driverName = driverName;
    this.fileNameExtensions = fileNameExtensions;
    if (Gdal.isAvailable()) {
      this.available = ogr.GetDriverByName(driverName) != null;
      if (this.available) {
        // final RecordStoreRecordAndGeometryWriterFactory writerFactory = new
        // RecordStoreRecordAndGeometryWriterFactory(
        // name, mediaType, true, true, fileNameExtensions);
        // IoFactoryRegistry.getInstance().addFactory(writerFactory);

        for (final String extension : fileNameExtensions) {
          this.urlPatterns.add("file:/(//)?.*." + extension + "/?");
          this.urlPatterns.add("folderconnection:/(//)?.*." + extension + "/?");

        }
      }
    } else {
      this.available = false;
    }
  }

  @Override
  public OgrRecordStore createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>(connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    final OgrRecordStore recordStore = create(this.driverName, file);
    RecordStoreFactoryRegistry.setConnectionProperties(recordStore, properties);
    return recordStore;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return this.fileNameExtensions;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return this.urlPatterns;
  }

  @Override
  public boolean isAvailable() {
    return this.available;
  }
}
