package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.util.ExceptionUtil;

public class DataObjectStoreSchema extends AbstractObjectWithProperties {
  private AbstractDataObjectStore dataStore;

  private Map<String, DataObjectMetaData> metaDataCache = new TreeMap<String, DataObjectMetaData>();

  private String path;

  public DataObjectStoreSchema(final AbstractDataObjectStore dataObjectStore,
    final String path) {
    this.dataStore = dataObjectStore;
    this.path = path;
  }

  public void addMetaData(final DataObjectMetaData metaData) {
    addMetaData(metaData.getPath(), metaData);
  }

  protected void addMetaData(final String typePath,
    final DataObjectMetaData metaData) {
    metaDataCache.put(typePath, metaData);
  }

  @Override
  @PreDestroy
  public void close() {
    if (metaDataCache != null) {
      for (final DataObjectMetaData metaData : metaDataCache.values()) {
        metaData.destroy();
      }
      metaDataCache.clear();
    }
    dataStore = null;
    metaDataCache = null;
    path = null;
    super.close();
  }

  public synchronized DataObjectMetaData findMetaData(final String typePath) {
    final DataObjectMetaData metaData = metaDataCache.get(typePath);
    return metaData;
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      return dataStore.getGeometryFactory();
    } else {
      return geometryFactory;
    }
  }

  public synchronized DataObjectMetaData getMetaData(String typePath) {
    typePath = typePath.toUpperCase();
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      if (metaDataCache.isEmpty()) {
        refreshMetaData();
      }
      final DataObjectMetaData metaData = metaDataCache.get(typePath.toUpperCase());
      return metaData;
    } else {
      return null;
    }
  }

  protected Map<String, DataObjectMetaData> getMetaDataCache() {
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
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<String>(metaDataCache.keySet());
  }

  public List<DataObjectMetaData> getTypes() {
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<DataObjectMetaData>(metaDataCache.values());
  }

  public void refreshMetaData() {
    final Collection<DataObjectStoreExtension> extensions = dataStore.getDataStoreExtensions();
    for (final DataObjectStoreExtension extension : extensions) {
      try {
        if (extension.isEnabled(dataStore)) {
          extension.preProcess(this);
        }
      } catch (final Throwable e) {
        ExceptionUtil.log(extension.getClass(), "Unable to pre-process schema "
          + this, e);
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

  @Override
  public String toString() {
    return path;
  }
}
