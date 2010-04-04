package com.revolsys.jump.ecsv;

import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.ecsv.service.client.EcsvDataObjectStore;
import com.revolsys.jump.model.DataObjectFeature;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.datastore.DataStoreMetadata;
import com.vividsolutions.jump.feature.FeatureSchema;

public class EcsvDataStoreMetaData implements DataStoreMetadata {

  private String schema;

  private EcsvDataObjectStore client;

  public EcsvDataStoreMetaData(
    final EcsvDataObjectStore client, final String schema) {
    this.client = client;
    this.schema = schema;
  }

  public String[] getDatasetNames() {
    List<QName> typeNames = client.getTypeNames(schema);
    if (typeNames != null) {
      String[] names = new String[typeNames.size()];
      for (int i = 0; i < typeNames.size(); i++) {
        names[i] = typeNames.get(i).getLocalPart();
      }
      return names;
    } else {
      return new String[0];
    }
  }

  public Envelope getExtents(final String datasetName,
    final String attributeName) {
    return new Envelope();
  }

  public String[] getGeometryAttributeNames(final String datasetName) {
    return new String[] {
      "GEOMETRY"
    };
  }

  public FeatureSchema getFeatureSchema(final String typeName,
    final String string) {
    // TODO Auto-generated method stub
    return null;
  }

  public EcsvDataObjectStore getClient() {
    return client;
  }

}
