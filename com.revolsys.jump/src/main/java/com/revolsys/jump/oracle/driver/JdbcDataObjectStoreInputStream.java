package com.revolsys.jump.oracle.driver;

import java.sql.SQLException;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.BaseFeatureInputStream;

/**
 * Reads features from an Oracle database.
 */
public class JdbcDataObjectStoreInputStream extends BaseFeatureInputStream {
  private FeatureSchema featureSchema;

  private JdbcDataObjectStore dataStore;

  private QName typeName;

  private Geometry geometry;

  private Reader<DataObject> featureReader;

  private Iterator<DataObject> featureIterator;

  private String condition;

  private GeometryFactory geometryFactory;

  public JdbcDataObjectStoreInputStream(
    final JdbcDataObjectStore dataStore,
    GeometryFactory geometryFactory,
    final QName typeName,
    final FeatureSchema featureSchema,
    final Geometry geometry,
    final String condition) {
    this.dataStore = dataStore;
    this.typeName = typeName;
    this.geometry = geometry;
    this.featureSchema = featureSchema;
    this.condition = condition;
    this.geometryFactory = geometryFactory;
    int srid = geometry.getSRID();
    if (srid == 0 && geometryFactory != null) {
      geometry.setSRID(geometryFactory.getSRID());
    }
    if (condition == null || condition.trim().length() == 1) {
      featureReader = dataStore.query(typeName, geometry);
    } else {
      featureReader = dataStore.query(typeName, geometry, condition);
    }
    featureIterator = featureReader.iterator();
  }

  protected Feature readNext()
    throws Exception {
    if (featureIterator.hasNext()) {
      final Feature feature = (Feature)featureIterator.next();
      if (geometryFactory != null) {
        Geometry geometry = feature.getGeometry();
        geometry = ProjectionFactory.convert(geometry, geometryFactory);
        feature.setGeometry(geometry);
      }
      return feature;
    } else {
      return null;
    }
  }

  public void close()
    throws SQLException {
    featureReader.close();
  }

  public FeatureSchema getFeatureSchema() {
    return featureSchema;

  }
}
