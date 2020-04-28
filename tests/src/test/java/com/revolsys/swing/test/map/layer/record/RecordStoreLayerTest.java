package com.revolsys.swing.test.map.layer.record;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.NewProxyLayerRecord;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;

public class RecordStoreLayerTest {
  private static final String CHANGED_NAME = "Changed Name";

  private static final String DEFAULT_NAME = "A Record";

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed2d(3587, 1000.0,
    1000.0);

  private static final PathName TEST = PathName.newPathName("TEST");

  private static File testDirectory;

  @AfterClass
  public static void afterSuite() {
    FileUtil.deleteDirectory(testDirectory, true);
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
    FileUtil.deleteDirectory(testDirectory, false);
  }

  protected List<LayerRecord> assertGetRecords(final int expectedRecordCount) {
    final Query query = new Query(TEST);
    return assertGetRecords(query, expectedRecordCount);
  }

  private List<LayerRecord> assertGetRecords(final Query query, final int expectedRecordCount) {
    final List<LayerRecord> records = this.layer.getRecords(query);
    Assert.assertEquals("Size", expectedRecordCount, records.size());
    final int actualRecordCount = this.layer.getRecordCount(query);
    Assert.assertEquals("Count", expectedRecordCount, actualRecordCount);
    return records;
  }

  public LayerRecord assertRecordAtIndex(final int count, final List<LayerRecord> records,
    final int index, final LayerRecord expectedRecord) {
    Assert.assertEquals("queryCount", count, records.size());
    final LayerRecord actualRecord = records.get(index);

    final boolean same1 = expectedRecord.isSame(actualRecord);
    Assert.assertTrue("record same", same1);

    final boolean same2 = actualRecord.isSame(expectedRecord);
    Assert.assertTrue("record same", same2);

    return actualRecord;
  }

  public LayerRecord assertRecordAtIndex(final int count, final List<LayerRecord> records,
    final int index, final LayerRecord expectedRecord, final Class<?> expectedClass) {
    final LayerRecord actualRecord = assertRecordAtIndex(count, records, index, expectedRecord);
    Assert.assertEquals("Class", expectedClass, actualRecord.getClass());
    return actualRecord;
  }

  public void assertRecordCounts(final int newRecordCount, final int persistedRecordCount,
    final int modifiedRecordCount, final int deletedRecordCount) {
    final int actualRecordCountPersisted = this.layer.getRecordCountPersisted();
    Assert.assertEquals("PERSISTED Count", persistedRecordCount, actualRecordCountPersisted);

    final int actualRecordCountNew = this.layer.getRecordCountNew();
    Assert.assertEquals("NEW Count", newRecordCount, actualRecordCountNew);

    final int actualRecordCountModified = this.layer.getRecordCountModified();
    Assert.assertEquals("MODIFIED Count", modifiedRecordCount, actualRecordCountModified);

    final int actualRecordCountDeleted = this.layer.getRecordCountDeleted();
    Assert.assertEquals("DELETED Count", deletedRecordCount, actualRecordCountDeleted);

    final int actualLayerRecordCount = this.layer.getRecordCount();
    Assert.assertEquals("Record Count", newRecordCount + persistedRecordCount - deletedRecordCount,
      actualLayerRecordCount);
  }

  public void assertRecordState(final Record record, final RecordState expectedState) {
    final RecordState actualState = record.getState();
    Assert.assertEquals("Record State", expectedState, actualState);
  }

  public void assertRecordValue(final Record record, final String fieldName,
    final Object expectedValue) {
    final Object actualValue = record.getValue(fieldName);
    Assert.assertEquals("Record Value", expectedValue, actualValue);
  }

  @Before
  public void beforeTest() {
    FileUtil.deleteDirectory(testDirectory, false);
    final File testFile = new File(testDirectory, "test.gdb");
    this.recordStore = FileGdbRecordStoreFactory.newRecordStore(testFile);
    this.recordStore.initialize();

    this.recordDefinition = new RecordDefinitionBuilder(TEST) //
      .addField("NAME", DataTypes.STRING, 50) //
      .addField("COUNT", DataTypes.INT) //
      .addField("GEOMETRY", GeometryDataTypes.POINT) //
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
  //
  // @Test
  // public void testDeleteRecordCancelChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // // Delete the record and verify that it's deleted
  // this.layer.deleteRecord(testRecord);
  // assertRecordCounts(0, 1, 0, 1);
  // assertGetRecords(0);
  // assertRecordState(testRecord, RecordState.DELETED);
  //
  // // Cancel the changes and verify that it hasn't been deleted.
  // this.layer.cancelChanges();
  // assertRecordCounts(0, 1, 0, 0);
  // final List<LayerRecord> records = assertGetRecords(1);
  // final LayerRecord actualRecord = assertRecordAtIndex(1, records, 0,
  // testRecord,
  // IdentifierProxyLayerRecord.class);
  // assertRecordState(testRecord, RecordState.PERSISTED);
  // assertRecordState(actualRecord, RecordState.PERSISTED);
  // }
  //
  // @Test
  // public void testDeleteRecordSaveChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // // Delete the record and verify that it's deleted
  // this.layer.deleteRecord(testRecord);
  // assertRecordCounts(0, 1, 0, 1);
  // assertGetRecords(0);
  // assertRecordState(testRecord, RecordState.DELETED);
  //
  // // Cancel the changes and verify that it hasn't been deleted.
  // this.layer.saveChanges();
  // assertRecordCounts(0, 0, 0, 0);
  // assertGetRecords(0);
  // assertRecordState(testRecord, RecordState.DELETED);
  //
  // }
  //
  // @Test
  // public void testFilterRecordModifiedMatches() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // testRecord.setValue("NAME", CHANGED_NAME);
  // assertRecordState(testRecord, RecordState.MODIFIED);
  // assertRecordCounts(0, 1, 1, 0);
  // assertGetRecords(new Query(TEST).and(Q.equal("NAME", CHANGED_NAME)), 1);
  // assertGetRecords(new Query(TEST).and(Q.equal("NAME", DEFAULT_NAME)), 0);
  // }
  //
  // @Test
  // public void testModifiyRecordCancelChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // testRecord.setValue("NAME", CHANGED_NAME);
  // assertRecordState(testRecord, RecordState.MODIFIED);
  // assertRecordCounts(0, 1, 1, 0);
  // final List<LayerRecord> records1 = assertGetRecords(1);
  // assertRecordAtIndex(1, records1, 0, testRecord,
  // IdentifierProxyLayerRecord.class);
  //
  // // Save the record and verify that it's modified
  // this.layer.cancelChanges();
  // assertRecordCounts(0, 1, 0, 0);
  // final List<LayerRecord> records2 = assertGetRecords(1);
  // final LayerRecord actualRecord = assertRecordAtIndex(1, records2, 0,
  // testRecord,
  // IdentifierProxyLayerRecord.class);
  // assertRecordState(testRecord, RecordState.PERSISTED);
  // assertRecordState(actualRecord, RecordState.PERSISTED);
  // assertRecordValue(testRecord, "NAME", DEFAULT_NAME);
  // assertRecordValue(actualRecord, "NAME", DEFAULT_NAME);
  // }
  //
  // @Test
  // public void testModifiyRecordDeleteCancelChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // testRecord.setValue("NAME", CHANGED_NAME);
  // assertRecordState(testRecord, RecordState.MODIFIED);
  //
  // // Delete the record and verify that it's deleted
  // this.layer.deleteRecord(testRecord);
  // assertRecordCounts(0, 1, 0, 1);
  // assertGetRecords(0);
  // assertRecordState(testRecord, RecordState.DELETED);
  // assertRecordValue(testRecord, "NAME", DEFAULT_NAME);
  //
  // // Cancel the changes and verify that it hasn't been deleted.
  // this.layer.cancelChanges();
  // assertRecordCounts(0, 1, 0, 0);
  // final List<LayerRecord> records = assertGetRecords(1);
  // final LayerRecord actualRecord = assertRecordAtIndex(1, records, 0,
  // testRecord,
  // IdentifierProxyLayerRecord.class);
  // assertRecordState(testRecord, RecordState.PERSISTED);
  // assertRecordState(actualRecord, RecordState.PERSISTED);
  // assertRecordValue(testRecord, "NAME", DEFAULT_NAME);
  // assertRecordValue(actualRecord, "NAME", DEFAULT_NAME);
  // }
  //
  // @Test
  // public void testModifiyRecordSaveChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  //
  // testRecord.setValue("NAME", CHANGED_NAME);
  // assertRecordState(testRecord, RecordState.MODIFIED);
  // assertRecordCounts(0, 1, 1, 0);
  // final List<LayerRecord> records1 = assertGetRecords(1);
  // assertRecordAtIndex(1, records1, 0, testRecord,
  // IdentifierProxyLayerRecord.class);
  //
  // // Save the record and verify that it's modified
  // this.layer.saveChanges();
  // assertRecordCounts(0, 1, 0, 0);
  // final List<LayerRecord> records2 = assertGetRecords(1);
  // final LayerRecord actualRecord = assertRecordAtIndex(1, records2, 0,
  // testRecord,
  // IdentifierProxyLayerRecord.class);
  // assertRecordState(testRecord, RecordState.PERSISTED);
  // assertRecordState(actualRecord, RecordState.PERSISTED);
  // assertRecordValue(testRecord, "NAME", CHANGED_NAME);
  // assertRecordValue(actualRecord, "NAME", CHANGED_NAME);
  // }

  private LayerRecord testNewRecord() {
    assertRecordCounts(0, 0, 0, 0);

    final LayerRecord testRecord = newTestRecord(DEFAULT_NAME, 10,
      GEOMETRY_FACTORY.point(12222000.001, 467000.999));
    assertRecordState(testRecord, RecordState.NEW);
    assertRecordCounts(1, 0, 0, 0);

    final List<LayerRecord> records = assertGetRecords(1);
    final LayerRecord actualRecord = assertRecordAtIndex(1, records, 0, testRecord,
      NewProxyLayerRecord.class);
    assertRecordState(actualRecord, RecordState.NEW);
    Assert.assertTrue("Has Changes", this.layer.isHasChanges());

    return testRecord;
  }

  @Test
  public void testNewRecordCancelChanges() {
    final LayerRecord testRecord = testNewRecord();

    this.layer.cancelChanges();
    assertRecordCounts(0, 0, 0, 0);
    assertRecordState(testRecord, RecordState.DELETED);
    Assert.assertFalse("Has Changes", this.layer.isHasChanges());
  }

  @Test
  public void testNewRecordChangeValues() {
    final LayerRecord testRecord = testNewRecord();

    testRecord.setValue("NAME", CHANGED_NAME);

    final List<LayerRecord> records2 = assertGetRecords(1);
    final LayerRecord actualRecord = assertRecordAtIndex(1, records2, 0, testRecord);
    assertRecordCounts(1, 0, 0, 0);
    assertRecordState(testRecord, RecordState.NEW);
    assertRecordState(actualRecord, RecordState.NEW);
    Assert.assertTrue("Has Changes", this.layer.isHasChanges());
  }

  @Test
  public void testNewRecordDelete() {
    final LayerRecord testRecord = testNewRecord();

    this.layer.deleteRecord(testRecord);
    assertRecordCounts(0, 0, 0, 0);
    assertRecordState(testRecord, RecordState.DELETED);
    Assert.assertFalse("Has Changes", this.layer.isHasChanges());
  }

  // @Test
  // public void testNewRecordSaveChanges() {
  // final LayerRecord testRecord = testNewRecord();
  //
  // this.layer.saveChanges();
  // final List<LayerRecord> records2 = assertGetRecords(1);
  // final LayerRecord actualRecord = assertRecordAtIndex(1, records2, 0,
  // testRecord,
  // IdentifierProxyLayerRecord.class);
  // assertRecordCounts(0, 1, 0, 0);
  // assertRecordState(testRecord, RecordState.PERSISTED);
  // assertRecordState(actualRecord, RecordState.PERSISTED);
  // Assert.assertFalse("Has Changes", this.layer.isHasChanges());
  // }
}
