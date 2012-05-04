package com.revolsys.gis.data.io;

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
  public synchronized DataObjectMetaData findMetaData(final String typePath) {
    DataObjectMetaData metaData = super.findMetaData(typePath);
    if (metaData == null) {
      metaData = schema.findMetaData(typePath);
      if (metaData != null) {
        metaData = new DataObjectMetaDataImpl(getDataObjectStore(), this,
          metaData);
        addMetaData(typePath, metaData);
      }
    }
    return metaData;
  }

  @Override
  public synchronized DataObjectMetaData getMetaData(final String typePath) {
    DataObjectMetaData metaData = findMetaData(typePath);
    if (metaData == null) {
      metaData = schema.getMetaData(typePath);
      if (metaData != null) {
        metaData = new DataObjectMetaDataImpl(getDataObjectStore(), this,
          metaData);
        addMetaData(typePath, metaData);
      }
    }
    return metaData;
  }
}
