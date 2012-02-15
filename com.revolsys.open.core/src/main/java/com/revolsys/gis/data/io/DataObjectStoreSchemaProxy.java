package com.revolsys.gis.data.io;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;

public class DataObjectStoreSchemaProxy extends DataObjectStoreSchema {
  private final DataObjectStoreSchema schema;

  public DataObjectStoreSchemaProxy(
    final AbstractDataObjectStore dataObjectStore, final String name,
    final DataObjectStoreSchema schema) {
    super(dataObjectStore, name);
    this.schema = schema;
  }

  @Override
  public synchronized DataObjectMetaData findMetaData(final QName typeName) {
    DataObjectMetaData metaData = super.findMetaData(typeName);
    if (metaData == null) {
      metaData = schema.findMetaData(typeName);
      if (metaData != null) {
        metaData = new DataObjectMetaDataImpl(getDataObjectStore(), this,
          metaData);
        addMetaData(typeName, metaData);
      }
    }
    return metaData;
  }

  @Override
  public synchronized DataObjectMetaData getMetaData(final QName typeName) {
    DataObjectMetaData metaData = findMetaData(typeName);
    if (metaData == null) {
      metaData = schema.getMetaData(typeName);
      if (metaData != null) {
        metaData = new DataObjectMetaDataImpl(getDataObjectStore(), this,
          metaData);
        addMetaData(typeName, metaData);
      }
    }
    return metaData;
  }
}
