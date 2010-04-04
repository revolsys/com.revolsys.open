package com.revolsys.jump.ecsv;

import java.util.Iterator;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.ecsv.service.client.EcsvDataObjectStore;
import com.revolsys.jump.model.DataObjectFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.BaseFeatureInputStream;

/**
 * Reads features from an Oracle database.
 */
public class EcsvFeatureInputStream extends BaseFeatureInputStream {

  private FeatureSchema featureSchema;

  private Iterator<DataObject> iterator;

  private Reader<DataObject> reader;

  private Feature currentFeature;

  public EcsvFeatureInputStream(
    final EcsvDataObjectStore client, final QName typeName,
    final FilterQuery query) {
    Geometry geometry = query.getFilterGeometry();

    reader = client.query(typeName, geometry);
    iterator = reader.iterator();
    if (iterator.hasNext()) {
      currentFeature = (Feature)iterator.next();
      featureSchema = currentFeature.getSchema();
    }
  }

  public void close() throws Exception {
    currentFeature = null;
    reader.close();
    reader = null;
    iterator = null;
  }

  public FeatureSchema getFeatureSchema() {
    return featureSchema;
  }

  protected Feature readNext() throws Exception {
    if (currentFeature != null) {
      Feature feature = currentFeature;
      if (iterator.hasNext()) {
        currentFeature = (Feature)iterator.next();
      } else {
        currentFeature = null;
      }
      return feature;
    } else {
      return null;
    }
  }
}
