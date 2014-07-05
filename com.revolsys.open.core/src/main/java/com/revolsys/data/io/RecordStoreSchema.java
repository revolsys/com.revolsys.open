package com.revolsys.data.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.ExceptionUtil;

public class RecordStoreSchema extends AbstractObjectWithProperties {

  private Reference<AbstractRecordStore> dataStore;

  private Map<String, RecordDefinition> metaDataCache = null;

  private String path;

  public RecordStoreSchema() {
  }

  public RecordStoreSchema(final AbstractRecordStore dataStore,
    final String path) {
    this.dataStore = new WeakReference<AbstractRecordStore>(dataStore);
    this.path = path;
  }

  public void addMetaData(final RecordDefinition metaData) {
    addMetaData(metaData.getPath(), metaData);
  }

  protected void addMetaData(final String typePath,
    final RecordDefinition metaData) {
    final Map<String, RecordDefinition> metaDataCache = getMetaDataCache();
    metaDataCache.put(typePath.toUpperCase(), metaData);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (metaDataCache != null) {
      for (final RecordDefinition metaData : metaDataCache.values()) {
        metaData.destroy();
      }
    }
    dataStore = null;
    metaDataCache = null;
    path = null;
    super.close();
  }

  public synchronized RecordDefinition findMetaData(final String typePath) {
    final Map<String, RecordDefinition> metaDataCache = getMetaDataCache();
    final RecordDefinition metaData = metaDataCache.get(typePath);
    return metaData;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getDataStore() {
    if (dataStore == null) {
      return null;
    } else {
      return (V)dataStore.get();
    }
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final AbstractRecordStore dataStore = getDataStore();
      if (dataStore == null) {
        return GeometryFactory.floating3();
      } else {
        return dataStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public synchronized RecordDefinition getMetaData(String typePath) {
    typePath = typePath.toUpperCase();
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      final Map<String, RecordDefinition> metaDataCache = getMetaDataCache();
      final RecordDefinition metaData = metaDataCache.get(typePath.toUpperCase());
      return metaData;
    } else {
      return null;
    }
  }

  protected synchronized Map<String, RecordDefinition> getMetaDataCache() {
    if (metaDataCache == null) {
      refreshMetaData();
    }
    return metaDataCache;
  }

  public String getName() {
    final String path = getPath();
    return PathUtil.getName(path);
  }

  public String getPath() {
    return path;
  }

  public List<String> getTypeNames() {
    final Map<String, RecordDefinition> metaDataCache = getMetaDataCache();
    return new ArrayList<String>(metaDataCache.keySet());
  }

  public List<RecordDefinition> getTypes() {
    final Map<String, RecordDefinition> metaDataCache = getMetaDataCache();
    return new ArrayList<RecordDefinition>(metaDataCache.values());
  }

  public boolean isInitialized() {
    return metaDataCache != null;
  }

  public synchronized void refreshMetaData() {
    metaDataCache = new TreeMap<>();
    final AbstractRecordStore dataStore = getDataStore();
    if (dataStore != null) {
      final Collection<RecordStoreExtension> extensions = dataStore.getDataStoreExtensions();
      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(dataStore)) {
            extension.preProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to pre-process schema " + this, e);
        }
      }
      dataStore.loadSchemaRecordDefinitions(this, metaDataCache);
      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(dataStore)) {
            extension.postProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to post-process schema " + this, e);
        }
      }
    }
  }

  @Override
  public String toString() {
    return path;
  }
}
