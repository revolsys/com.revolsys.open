package com.revolsys.gis.esri.gdb.file;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.PathName;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;

public class FeatureDatasetTest {

  @Test
  public void testCreateGeodatabase() throws Exception {
    final PathName typePath = PathName.newPathName("/test/Point");
    final RecordDefinitionImpl newMetaData = new RecordDefinitionImpl(typePath);
    newMetaData.addField("id", DataTypes.INT, false);
    newMetaData.addField("name", DataTypes.STRING, 255, false);
    newMetaData.addField("geometry", DataTypes.POINT, true);
    newMetaData.setIdFieldName("id");
    final GeometryFactory geometryFactory = GeometryFactory.floating2d(4326);
    newMetaData.setGeometryFactory(geometryFactory);

    final String datasetName = "target/Create.gdb";
    FileGdbRecordStore recordStore = FileGdbRecordStoreFactory
      .newRecordStore(new File(datasetName));
    try {
      recordStore.setCreateMissingTables(true);
      recordStore.setCreateMissingRecordStore(true);
      recordStore.initialize();
      recordStore.setDefaultSchema("test");
      Assert.assertEquals("Initial Schema Size", 1,
        recordStore.getRootSchema().getSchemas().size());
      final RecordDefinition recordDefinition = recordStore.getRecordDefinition(newMetaData);
      Assert.assertNotNull("Created Metadata", recordDefinition);

      final Record object = recordStore.newRecord(newMetaData);
      object.setIdentifier(Identifier.newIdentifier(1));
      object.setValue("name", "Paul Austin");
      object.setGeometryValue(geometryFactory.point(-122, 150));
      recordStore.insertRecord(object);
      for (final Record object2 : recordStore.getRecords(typePath)) {
        System.out.println(object2);
      }
      recordStore.close();

      recordStore = FileGdbRecordStoreFactory.newRecordStore(new File(datasetName));
      recordStore.initialize();
      recordStore.setDefaultSchema("test");
      final RecordStoreSchema schema = recordStore.getSchema("test");
      for (final RecordDefinition recordDefinition2 : schema.getRecordDefinitions()) {
        System.out.println(recordDefinition2);
      }
    } finally {
      recordStore.deleteGeodatabase();
    }
  }
}
