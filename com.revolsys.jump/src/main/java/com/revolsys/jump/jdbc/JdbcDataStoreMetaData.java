package com.revolsys.jump.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jump.model.DataObjectMetaDataFeatureSchema;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.datastore.DataStoreMetadata;
import com.vividsolutions.jump.datastore.SpatialReferenceSystemID;
import com.vividsolutions.jump.feature.FeatureSchema;

public class JdbcDataStoreMetaData implements DataStoreMetadata {
  private Map<String, SpatialReferenceSystemID> sridMap = new HashMap<String, SpatialReferenceSystemID>();

  private Envelope maxExtent;

  private String schema;

  private JdbcDataObjectStore dataStore;

  public JdbcDataStoreMetaData(
    final JdbcDataObjectStore dataStore,
    final String schema) {
    this.dataStore = dataStore;
    this.schema = schema;
  }

  public String[] getDatasetNames() {
    List<QName> typeNames = dataStore.getTypeNames(schema);
    String[] dataSetNames = new String[typeNames.size()];
    for (int i = 0; i < typeNames.size(); i++) {
      QName typeName = typeNames.get(i);
      dataSetNames[i] = typeName.getLocalPart();
    }
    return dataSetNames;
  }

  public String[] getColumnNames(
    final String datasetName) {
    DataObjectMetaData metaData = dataStore.getMetaData(new QName(schema,
      datasetName));
    if (metaData == null) {
      return new String[0];
    } else {
      List<String> names = metaData.getAttributeNames();
      return names.toArray(new String[names.size()]);
    }
  }

  public Envelope getExtents(
    final String datasetName,
    final String attributeName) {
    if (maxExtent != null) {
      return maxExtent;
    } else {
      return null;
    }
  }

  public FeatureSchema getFeatureSchema(
    final String datasetName,
    final String geometryColumn) {
    DataObjectMetaData metaData = dataStore.getMetaData(new QName(schema,
      datasetName));
    return new DataObjectMetaDataFeatureSchema(metaData, geometryColumn);
  }

  public String[] getGeometryAttributeNames(
    final String datasetName) {
    DataObjectMetaData metaData = dataStore.getMetaData(new QName(schema,
      datasetName));
    Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute == null) {
      return new String[0];
    } else {
      String attributeName = metaData.getGeometryAttributeName();
      return new String[] {
        attributeName
      };
    }
  }

  public Envelope getMaxExtent() {
    return maxExtent;
  }

  public void setMaxExtent(
    final Envelope maxExtent) {
    this.maxExtent = maxExtent;
  }

}
