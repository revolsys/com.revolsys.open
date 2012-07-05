package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;

public class DataObjectStoreSchema extends AbstractObjectWithProperties {
  private AbstractDataObjectStore dataObjectStore;

  private Map<String, DataObjectMetaData> metaDataCache = new TreeMap<String, DataObjectMetaData>();

  private String path;

  public DataObjectStoreSchema(final AbstractDataObjectStore dataObjectStore,
    final String path) {
    this.dataObjectStore = dataObjectStore;
    this.path = path;
  }

  public void addMetaData(final DataObjectMetaData metaData) {
    addMetaData(metaData.getPath(), metaData);
  }

  protected void addMetaData(
    final String typePath,
    final DataObjectMetaData metaData) {
    metaDataCache.put(typePath, metaData);
  }

  @PreDestroy
  public void close() {
    if (metaDataCache != null) {
      for (final DataObjectMetaData metaData : metaDataCache.values()) {
        metaData.destroy();
      }
      metaDataCache.clear();
    }
    dataObjectStore = null;
    metaDataCache = null;
    path = null;
    super.close();
  }

  public synchronized DataObjectMetaData findMetaData(final String typePath) {
    final DataObjectMetaData metaData = metaDataCache.get(typePath);
    return metaData;
  }

  public DataObjectStore getDataObjectStore() {
    return dataObjectStore;
  }

  public synchronized DataObjectMetaData getMetaData(final String typePath) {
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      if (metaDataCache.isEmpty()) {
        refreshMetaData();
      }
      final DataObjectMetaData metaData = metaDataCache.get(typePath);
      return metaData;
    } else {
      return null;
    }
  }

  protected Map<String, DataObjectMetaData> getMetaDataCache() {
    return metaDataCache;
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
    dataObjectStore.loadSchemaDataObjectMetaData(this, metaDataCache);
  }

  @Override
  public String toString() {
    return path;
  }

  public String getName() {
    String path = getPath();
    return PathUtil.getName(path);
  }
}
