package com.revolsys.jump.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.jump.model.DataObjectMetaDataFeatureSchema;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

@SuppressWarnings("serial")
public class DataObjectFeatureCollection extends FeatureDataset {
  private static final Map<DataObjectMetaData, FeatureSchema> SCHEMAS = new WeakHashMap<DataObjectMetaData, FeatureSchema>();

  private static FeatureSchema getSchema(final DataObjectMetaData metaData) {
    FeatureSchema schema = SCHEMAS.get(metaData);
    if (schema == null) {
      schema = new DataObjectMetaDataFeatureSchema(metaData,
        metaData.getGeometryAttributeName());
      SCHEMAS.put(metaData, schema);
    }
    return schema;
  }

  private static Collection<Feature> createFeatures(
    final Collection<DataObject> objects, final DataObjectMetaData metaData) {
    FeatureSchema schema = getSchema(metaData);
    List<Feature> features = new ArrayList<Feature>();
    for (DataObject object : objects) {
      BasicFeature feature = new BasicFeature(schema);
      for (int i = 0; i < schema.getAttributeCount(); i++) {
        feature.setAttribute(i, object.getValue(i));
      }
      features.add(feature);
    }
    return features;
  }

  public DataObjectFeatureCollection(final DataObjectMetaData metaData,
    final Collection<DataObject> objects) {
    super(createFeatures(objects, metaData), getSchema(metaData));
  }
}
