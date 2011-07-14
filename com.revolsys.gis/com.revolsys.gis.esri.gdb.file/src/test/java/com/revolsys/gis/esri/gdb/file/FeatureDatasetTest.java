package com.revolsys.gis.esri.gdb.file;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureDatasetTest {
  static {
    System.loadLibrary("EsriFileGdb");
  }

  @Test
  public void testCreateGeodatabase() throws Exception {
    final QName typeName = new QName("test", "Point");
    DataObjectMetaDataImpl newMetaData = new DataObjectMetaDataImpl(typeName);
    newMetaData.addAttribute("id", DataTypes.INT, false);
    newMetaData.addAttribute("name", DataTypes.STRING, 255, false);
    newMetaData.addAttribute("geometry", DataTypes.POINT, true);
    newMetaData.setIdAttributeName("id");
    final GeometryFactory geometryFactory = new GeometryFactory(4326, 1000, 1000);
    newMetaData.setGeometryFactory(geometryFactory);

    final String datasetName = "target/Create.gdb";
    final EsriFileGeodatabaseDataObjectStore dataStore = new EsriFileGeodatabaseDataObjectStore(
      datasetName);
    try {
      dataStore.setCreateMissingTables(true);
      dataStore.setCreateMissingGeodatabase(true);
      dataStore.initialize();
      Assert.assertEquals("Initial Schema Size", 1, dataStore.getSchemas()
        .size());
      final DataObjectMetaData metaData = dataStore.getMetaData(newMetaData);
      Assert.assertNotNull("Created Metadata", metaData);

      final DataObject object = dataStore.create(newMetaData);
      object.setIdValue(1);
      object.setValue("name", "Paul Austin");
      object.setGeometryValue(geometryFactory.createPoint(-122, 150));
      dataStore.insert(object);
      for (DataObject object2 : dataStore.query(typeName)) {
        System.out.println(object2);
      }
    } finally {
      dataStore.deleteGeodatabase();
    }
  }
}
