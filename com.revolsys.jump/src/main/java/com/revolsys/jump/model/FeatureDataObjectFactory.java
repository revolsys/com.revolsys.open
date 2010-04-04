package com.revolsys.jump.model;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class FeatureDataObjectFactory implements
  DataObjectFactory {
  private Map<DataObjectMetaData, DataObjectMetaDataFeatureSchema> schemas = new HashMap<DataObjectMetaData, DataObjectMetaDataFeatureSchema>();

  public DataObjectMetaDataFeatureSchema getFeatureSchema(
    final DataObjectMetaData type, final String geometryAttribute) {
    DataObjectMetaDataFeatureSchema schema = schemas.get(type);
    if (schema == null) {
      schema = new DataObjectMetaDataFeatureSchema(type, geometryAttribute);
      schemas.put(type, schema);
    }
    return schema;
  }

  public DataObjectFeature createDataObject(final DataObjectMetaData type) {
    return new DataObjectFeature(getFeatureSchema(type, null));
  }

  public DataObjectFeature createDataObject(final DataObjectMetaData type,
    final String geometryAttributeName) {
    return new DataObjectFeature(getFeatureSchema(type, geometryAttributeName));
  }

}
