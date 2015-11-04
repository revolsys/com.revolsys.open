package com.revolsys.swing.map.layer.record.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathName;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;

public class RecordStoreLayerTest {
  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed(3587, 2, 1000.0);

  private static final PathName TEST = PathName.newPathName("TEST");

  private static File testDirectory;

  @AfterClass
  public static void afterSuite() {
    testDirectory = null;
  }

  @BeforeClass
  public static void beforeSuite() {
    testDirectory = FileUtil.newTempDirectory(RecordStoreLayerTest.class.getName(), ".test");
  }

  private RecordStoreLayer layer;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  @After
  public void afterTest() {
    if (this.layer != null) {
      this.layer.close();
    }
    if (this.recordStore != null) {
      this.recordStore.close();
    }
    this.recordStore = null;
    this.recordDefinition = null;
    this.layer = null;
  }

  public void assertRecordAtIndex(final int count, final List<LayerRecord> records, final int index,
    final LayerRecord expectedRecord) {
    Assert.assertEquals("queryCount", count, records.size());
    final LayerRecord actualRecord = records.get(index);

    final boolean same1 = expectedRecord.isSame(actualRecord);
    Assert.assertTrue("record same", same1);

    final boolean same2 = actualRecord.isSame(expectedRecord);
    Assert.assertTrue("record same", same2);
  }

  public void assertRecordCounts(final int newRecordCount, final int persistedRecordCount,
    final int modifiedRecordCount, final int deletedRecordCount) {
    Assert.assertEquals("PERSISTED Count", persistedRecordCount,
      this.layer.getRecordCountPersisted());
    Assert.assertEquals("NEW Count", newRecordCount, this.layer.getRecordCountNew());
    Assert.assertEquals("MODIFIED Count", modifiedRecordCount, this.layer.getRecordCountModified());
    Assert.assertEquals("DELETED Count", deletedRecordCount, this.layer.getRecordCountDeleted());
    final int layerRecordCount = this.layer.getRecordCount();
    Assert.assertEquals("Record Count", newRecordCount + persistedRecordCount - deletedRecordCount,
      layerRecordCount);
  }

  @Before
  public void beforeTest() {
    this.recordStore = FileGdbRecordStoreFactory
      .newRecordStore(new File(testDirectory, "test.gdb"));
    this.recordStore.initialize();

    this.recordDefinition = new RecordDefinitionBuilder(TEST) //
      .addField("NAME", DataTypes.STRING) //
      .addField("COUNT", DataTypes.INT) //
      .addField("GEOMETRY", DataTypes.POINT) //
      .setGeometryFactory(GEOMETRY_FACTORY) //
      .getRecordDefinition();

    this.recordStore.getRecordDefinition(this.recordDefinition);
    this.layer = new RecordStoreLayer(this.recordStore, TEST, true);
    this.layer.initialize();
    this.layer.setEditable(true);
  }

  public LayerRecord newTestRecord(final String name, final int count, final Point point) {
    final Map<String, Object> newValues = Maps.<String, Object> buildLinkedHash()
      .add("NAME", name)
      .add("COUNT", count)
      .add("GEOMETRY", point);
    final LayerRecord testRecord = this.layer.newLayerRecord(newValues);
    return testRecord;
  }

  @Test
  public void testNewRecordCancelChanges() {
    assertRecordCounts(0, 0, 0, 0);
    final LayerRecord testRecord = newTestRecord("CANCEL", 10,
      GEOMETRY_FACTORY.point(12222000.001, 467000.999));
    assertRecordCounts(1, 0, 0, 0);
    final List<LayerRecord> records = this.layer.getRecords(TEST);
    assertRecordAtIndex(1, records, 0, testRecord);
    this.layer.cancelChanges();
    assertRecordCounts(0, 0, 0, 0);
  }
}
