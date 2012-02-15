package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.xml.QNameComparator;

public class DataObjectStoreSchema {
  private final AbstractDataObjectStore dataObjectStore;

  private final Map<QName, DataObjectMetaData> metaDataCache = new TreeMap<QName, DataObjectMetaData>(
    new QNameComparator());

  private final String name;

  private Map<QName, Object> properties = new HashMap<QName, Object>();

  public DataObjectStoreSchema(final AbstractDataObjectStore dataObjectStore,
    final String name) {
    this.dataObjectStore = dataObjectStore;
    this.name = name;
  }

  public void addMetaData(final DataObjectMetaData metaData) {
    addMetaData(metaData.getName(), metaData);
  }

  protected void addMetaData(
    final QName typeName,
    final DataObjectMetaData metaData) {
    metaDataCache.put(typeName, metaData);
  }

  public synchronized DataObjectMetaData findMetaData(final QName typeName) {
    final DataObjectMetaData metaData = metaDataCache.get(typeName);
    return metaData;
  }

  public DataObjectStore getDataObjectStore() {
    return dataObjectStore;
  }

  public synchronized DataObjectMetaData getMetaData(final QName typeName) {
    if (typeName.getNamespaceURI().equals(name)) {
      if (metaDataCache.isEmpty()) {
        refreshMetaData();
      }
      final DataObjectMetaData metaData = metaDataCache.get(typeName);
      return metaData;
    } else {
      return null;
    }
  }

  protected Map<QName, DataObjectMetaData> getMetaDataCache() {
    return metaDataCache;
  }

  public String getName() {
    return name;
  }

  public Map<QName, Object> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <V extends Object> V getProperty(final QName name) {
    return (V)properties.get(name);
  }

  public List<QName> getTypeNames() {
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<QName>(metaDataCache.keySet());
  }

  public List<DataObjectMetaData> getTypes() {
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<DataObjectMetaData>(metaDataCache.values());
  }

  protected void refreshMetaData() {
    dataObjectStore.loadSchemaDataObjectMetaData(this, metaDataCache);
  }

  public void setProperties(final Map<QName, Object> properties) {
    this.properties = properties;
  }

  public void setProperty(final QName name, final Object value) {
    properties.put(name, value);
  }

  @Override
  public String toString() {
    return name;
  }
}
