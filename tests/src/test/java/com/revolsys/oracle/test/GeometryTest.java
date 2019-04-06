package com.revolsys.oracle.test;

import java.util.Collections;

import org.jeometry.common.datatype.DataType;
import org.jeometry.common.io.PathName;
import org.junit.Assert;
import org.junit.Test;

import com.revolsys.core.test.geometry.test.model.GeometryTestUtil;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public class GeometryTest {

  private final RecordStore recordStore = RecordStore
    .newRecordStore("jdbc:oracle:thin:@//192.168.1.105:1521/TEST", "ORACLE_TEST", "test");

  public GeometryTest() {
    this.recordStore.initialize();
  }

  private void doWriteTest(final DataType geometryType, final int axisCount, final int partCount,
    final int ringCount) {
    final GeometryFactory sourceGeometryFactory = GeometryFactory.floating(3857, axisCount);
    String typeName = geometryType.getName().toUpperCase();
    typeName = typeName.replace("MULTI", "MULTI_");
    final PathName typePath = PathName.newPathName("/ORACLE_TEST/" + typeName + axisCount);
    Geometry geometry = GeometryTestUtil.geometry(sourceGeometryFactory, geometryType, axisCount,
      partCount, ringCount);
    if (geometry instanceof Polygonal) {
      geometry = geometry.toCounterClockwise();
    }
    try (
      Transaction transaction = this.recordStore.newTransaction(Propagation.REQUIRES_NEW)) {
      final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(typePath);
      final Record record = this.recordStore.newRecord(typePath,
        Collections.singletonMap("GEOMETRY", geometry));
      try (
        Writer<Record> writer = this.recordStore.newRecordWriter()) {
        writer.write(record);
      }
      final Identifier identifier = record.getIdentifier();
      final Record savedRecord = this.recordStore.getRecord(typePath, identifier);
      Assert.assertNotNull("Saved record", savedRecord);
      final Geometry savedGeometry = savedRecord.getGeometry();
      final GeometryFactory tableGeometryFactory = recordDefinition.getGeometryFactory();
      final Geometry expectedGeometry = geometry.convertGeometry(tableGeometryFactory);
      com.revolsys.geometry.util.Assert.equals("Saved geometry",
        savedGeometry.equalsExact(expectedGeometry), expectedGeometry, savedGeometry);
      transaction.setRollbackOnly();
    }
  }

  @Test
  public void lineString() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      doWriteTest(GeometryDataTypes.LINE_STRING, axisCount, 1, 1);
    }
  }

  @Test
  public void multiLineString() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int partCount = 1; partCount < 4; partCount++) {
        doWriteTest(GeometryDataTypes.MULTI_LINE_STRING, axisCount, partCount, 1);
      }
    }
  }

  @Test
  public void multiPoint() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int partCount = 1; partCount < 4; partCount++) {
        doWriteTest(GeometryDataTypes.MULTI_POINT, axisCount, partCount, 1);
      }
    }
  }

  @Test
  public void multiPolygon() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int partCount = 1; partCount < 4; partCount++) {
        for (int ringCount = 1; ringCount < 4; ringCount++) {
          doWriteTest(GeometryDataTypes.MULTI_POLYGON, axisCount, partCount, ringCount);
        }
      }
    }
  }

  @Test
  public void point() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      doWriteTest(GeometryDataTypes.POINT, axisCount, 1, 1);
    }
  }

  @Test
  public void polygon() {
    for (int axisCount = 2; axisCount < 4; axisCount++) {
      for (int ringCount = 1; ringCount < 4; ringCount++) {
        doWriteTest(GeometryDataTypes.POLYGON, axisCount, 1, ringCount);
      }
    }
  }
}
