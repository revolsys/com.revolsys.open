package com.revolsys.jump.oracle.driver;

import java.sql.SQLException;
import java.util.Iterator;

import javax.xml.namespace.QName;

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

  public JdbcDataObjectStoreInputStream(
    final JdbcDataObjectStore dataStore,
    final QName typeName,
    final FeatureSchema featureSchema,
    final Geometry geometry,
    final String condition) {
    this.dataStore = dataStore;
    this.typeName = typeName;
    this.geometry = geometry;
    this.featureSchema = featureSchema;
    this.condition = condition;
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
      return (Feature)featureIterator.next();
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
