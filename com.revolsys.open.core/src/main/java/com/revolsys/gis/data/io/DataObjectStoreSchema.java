package com.revolsys.gis.data.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.ExceptionUtil;

public class DataObjectStoreSchema extends AbstractObjectWithProperties {

  private Reference<AbstractDataObjectStore> dataStore;

  private Map<String, DataObjectMetaData> metaDataCache = null;

  private String path;

  public DataObjectStoreSchema() {
  }

  public DataObjectStoreSchema(final AbstractDataObjectStore dataStore,
    final String path) {
    this.dataStore = new WeakReference<AbstractDataObjectStore>(dataStore);
    this.path = path;
  }

  public void addMetaData(final DataObjectMetaData metaData) {
    addMetaData(metaData.getPath(), metaData);
  }

  protected void addMetaData(final String typePath,
    final DataObjectMetaData metaData) {
    final Map<String, DataObjectMetaData> metaDataCache = getMetaDataCache();
    metaDataCache.put(typePath.toUpperCase(), metaData);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (metaDataCache != null) {
      for (final DataObjectMetaData metaData : metaDataCache.values()) {
        metaData.destroy();
      }
    }
    dataStore = null;
    metaDataCache = null;
    path = null;
    super.close();
  }

  public synchronized DataObjectMetaData findMetaData(final String typePath) {
    final Map<String, DataObjectMetaData> metaDataCache = getMetaDataCache();
    final DataObjectMetaData metaData = metaDataCache.get(typePath);
    return metaData;
  }

  @SuppressWarnings("unchecked")
  public <V extends DataObjectStore> V getDataStore() {
    if (dataStore == null) {
      return null;
    } else {
      return (V)dataStore.get();
    }
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final AbstractDataObjectStore dataStore = getDataStore();
      if (dataStore == null) {
        return GeometryFactory.floating3();
      } else {
        return dataStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public synchronized DataObjectMetaData getMetaData(String typePath) {
    typePath = typePath.toUpperCase();
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      final Map<String, DataObjectMetaData> metaDataCache = getMetaDataCache();
      final DataObjectMetaData metaData = metaDataCache.get(typePath.toUpperCase());
      return metaData;
    } else {
      return null;
    }
  }

  protected synchronized Map<String, DataObjectMetaData> getMetaDataCache() {
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
    final Map<String, DataObjectMetaData> metaDataCache = getMetaDataCache();
    return new ArrayList<String>(metaDataCache.keySet());
  }

  public List<DataObjectMetaData> getTypes() {
    final Map<String, DataObjectMetaData> metaDataCache = getMetaDataCache();
    return new ArrayList<DataObjectMetaData>(metaDataCache.values());
  }

  public boolean isInitialized() {
    return metaDataCache != null;
  }

  public synchronized void refreshMetaData() {
    metaDataCache = new TreeMap<>();
    final AbstractDataObjectStore dataStore = getDataStore();
    if (dataStore != null) {
      final Collection<DataObjectStoreExtension> extensions = dataStore.getDataStoreExtensions();
      for (final DataObjectStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(dataStore)) {
            extension.preProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to pre-process schema " + this, e);
        }
      }
      dataStore.loadSchemaDataObjectMetaData(this, metaDataCache);
      for (final DataObjectStoreExtension extension : extensions) {
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
