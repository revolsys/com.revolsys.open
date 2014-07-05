package com.revolsys.gis.esri.gdb.file;

import java.io.File;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.data.io.RecordStoreSchema;
import com.revolsys.gis.data.model.Record;
import com.revolsys.gis.data.model.RecordMetaData;
import com.revolsys.gis.data.model.RecordMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public class FeatureDatasetTest {

  @Test
  public void testCreateGeodatabase() throws Exception {
    final String path = new QName("test", "Point");
    RecordMetaDataImpl newMetaData = new RecordMetaDataImpl(typePath);
    newMetaData.addAttribute("id", DataTypes.INT, false);
    newMetaData.addAttribute("name", DataTypes.STRING, 255, false);
    newMetaData.addAttribute("geometry", DataTypes.POINT, true);
    newMetaData.setIdAttributeName("id");
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(4326);
    newMetaData.setGeometryFactory(geometryFactory);

    final String datasetName = "target/Create.gdb";
    FileGdbRecordStore dataStore = FileGdbRecordStoreFactory.create(new File(
      datasetName));
    try {
      dataStore.setCreateMissingTables(true);
      dataStore.setCreateMissingDataStore(true);
      dataStore.initialize();
      dataStore.setDefaultSchema("test");
      Assert.assertEquals("Initial Schema Size", 1, dataStore.getSchemas()
        .size());
      final RecordMetaData recordDefinition = dataStore.getRecordDefinition(newMetaData);
      Assert.assertNotNull("Created Metadata", recordDefinition);

      final Record object = dataStore.create(newMetaData);
      object.setIdValue(1);
      object.setValue("name", "Paul Austin");
      object.setGeometryValue(geometryFactory.createPoint(-122, 150));
      dataStore.insert(object);
      for (Record object2 : dataStore.query(typePath)) {
        System.out.println(object2);
      }
      dataStore.close();

      dataStore = FileGdbRecordStoreFactory.create(new File(datasetName));
      dataStore.initialize();
      dataStore.setDefaultSchema("test");
      RecordStoreSchema schema = dataStore.getSchema("test");
      for (RecordMetaData recordDefinition2 : schema.getTypes()) {
        System.out.println(recordDefinition2);
      }
    } finally {
      dataStore.deleteGeodatabase();
    }
  }
}
