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
    FileGdbRecordStore recordStore = FileGdbRecordStoreFactory.create(new File(
      datasetName));
    try {
      recordStore.setCreateMissingTables(true);
      recordStore.setCreateMissingRecordStore(true);
      recordStore.initialize();
      recordStore.setDefaultSchema("test");
      Assert.assertEquals("Initial Schema Size", 1, recordStore.getSchemas()
        .size());
      final RecordMetaData recordDefinition = recordStore.getRecordDefinition(newMetaData);
      Assert.assertNotNull("Created Metadata", recordDefinition);

      final Record object = recordStore.create(newMetaData);
      object.setIdValue(1);
      object.setValue("name", "Paul Austin");
      object.setGeometryValue(geometryFactory.createPoint(-122, 150));
      recordStore.insert(object);
      for (Record object2 : recordStore.query(typePath)) {
        System.out.println(object2);
      }
      recordStore.close();

      recordStore = FileGdbRecordStoreFactory.create(new File(datasetName));
      recordStore.initialize();
      recordStore.setDefaultSchema("test");
      RecordStoreSchema schema = recordStore.getSchema("test");
      for (RecordMetaData recordDefinition2 : schema.getTypes()) {
        System.out.println(recordDefinition2);
      }
    } finally {
      recordStore.deleteGeodatabase();
    }
  }
}
