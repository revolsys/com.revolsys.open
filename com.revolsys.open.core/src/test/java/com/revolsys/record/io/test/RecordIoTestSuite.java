package com.revolsys.record.io.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.test.model.GeometryTestUtil;
import com.revolsys.geometry.test.model.TestUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.junit.RunnableTestCase;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.spring.resource.FileSystemResource;

import junit.framework.TestCase;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@SuiteClasses({
  GeoJsonIoTest.class, GmlIoTest.class, KmlIoTest.class, ShapefileIoTest.class, WktlIoTest.class,
  XBaseIoTest.class
})
public class RecordIoTestSuite {
  public static void addGeometryTestSuites(final TestSuite suite, final String namePrefix,
    final GeometryTestFunction<GeometryFactory, Geometry, DataType> testFunction) {
    final List<DataType> geometryDataTypes = Arrays.asList(DataTypes.POINT, DataTypes.LINE_STRING,
      DataTypes.POLYGON, DataTypes.MULTI_POINT, DataTypes.MULTI_LINE_STRING,
      DataTypes.MULTI_POLYGON);
    for (final DataType dataType : geometryDataTypes) {
      final TestSuite dataTypeSuite = new TestSuite(namePrefix + " " + dataType.toString());
      suite.addTest(dataTypeSuite);
      for (int axisCount = 2; axisCount < 5; axisCount++) {
        int maxGeometryCount = 1;
        if (dataType.toString().startsWith("Multi")) {
          maxGeometryCount = 3;
        }
        for (int geometryCount = 0; geometryCount <= maxGeometryCount; geometryCount++) {
          int maxVertexCount = 2;
          if (dataType.toString().contains("Line")) {
            maxVertexCount = 4;
          }
          for (int vertexCount = 2; vertexCount <= maxVertexCount; vertexCount++) {
            int maxRingCount = 0;
            if (dataType.toString().contains("Polygon")) {
              maxRingCount = 3;
            }
            if (geometryCount == 0) {
              maxRingCount = 0;
            }
            for (int ringCount = 0; ringCount <= maxRingCount; ringCount++) {
              final GeometryFactory geometryFactory = GeometryFactory.fixed(4326, axisCount,
                GeometryFactory.newScalesFixed(axisCount, 0.0));
              double delta = 1.0;
              if (geometryFactory.isProjected()) {
                delta = 1000.0;
              }
              final Geometry geometry = GeometryTestUtil.geometry(geometryFactory, dataType,
                geometryCount, ringCount, vertexCount, delta);
              String name = namePrefix + " " + dataType + " A=" + axisCount + " G=" + geometryCount;
              if (maxVertexCount > 2) {
                name += " V=" + vertexCount;
              }
              if (maxRingCount > 2) {
                name += " R=" + ringCount;
              }
              final TestCase testCase = new RunnableTestCase(name,
                () -> testFunction.apply(geometryFactory, geometry, dataType));
              dataTypeSuite.addTest(testCase);
            }
          }
        }
      }
    }
  }

  public static void addWriteReadTest(final TestSuite suite, final String prefix,
    final String fileExtension) {
    addGeometryTestSuites(suite, prefix, (geometryFactory, geometry, geometryDataType) -> {
      final String geometryTypeString = geometryDataType.toString();
      final File directory = new File("/tmp/revolsystest/io/" + fileExtension);
      final File file = new File(directory, geometryTypeString + "_"
        + geometryFactory.getAxisCount() + "_" + geometry.getVertexCount() + "." + fileExtension);
      file.delete();
      file.getParentFile().mkdirs();
      final FileSystemResource resource = new FileSystemResource(file);

      final RecordWriterFactory recordWriterFactory = IoFactory.factory(RecordWriterFactory.class,
        resource);

      final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
        PathName.newPathName(geometryTypeString));
      if (recordWriterFactory.isCustomFieldsSupported()) {
        recordDefinition.addField("BOOLEAN", DataTypes.BOOLEAN, true);
        recordDefinition.addField("BYTE", DataTypes.BYTE, true);
        recordDefinition.addField("SHORT", DataTypes.SHORT, true);
        recordDefinition.addField("INT", DataTypes.INT, true);
        recordDefinition.addField("LONG", DataTypes.LONG, true);
        recordDefinition.addField("FLOAT", DataTypes.FLOAT, 4, 3, true);
        recordDefinition.addField("DOUBLE", DataTypes.DOUBLE, 10, 9, true);
        recordDefinition.addField("STRING", DataTypes.STRING, true);
      }
      if (recordWriterFactory.isGeometrySupported()) {
        recordDefinition.addField("GEOMETRY", geometryDataType, true);
      }
      recordDefinition.setGeometryFactory(geometryFactory);

      final ArrayRecord record = new ArrayRecord(recordDefinition);
      record.setValue("BOOLEAN", true);
      record.setValue("BYTE", Byte.MAX_VALUE);
      record.setValue("SHORT", Short.MAX_VALUE);
      record.setValue("INT", Integer.MAX_VALUE);
      record.setValue("LONG", 999999999999999999L);
      record.setValue("FLOAT", 6.789);
      record.setValue("DOUBLE", 1.234567890);
      record.setValue("STRING", "test");
      record.setGeometryValue(geometry);

      doRecordWriteTest(resource, record);

      doRecordReadTest(resource, record);

      doGeometryReadTest(resource, record);
    });
  }

  private static void assertGeometry(final Geometry expectedGeometry,
    final Geometry actualGeometry) {
    Assert.assertEquals("Empty", expectedGeometry.isEmpty(), actualGeometry.isEmpty());
    if (!expectedGeometry.isEmpty()) {
      final GeometryFactory expectedGeometryFactory = expectedGeometry.getGeometryFactory();
      final GeometryFactory actualGeometryFactory = actualGeometry.getGeometryFactory();
      Assert.assertEquals("Geometry Factory", expectedGeometryFactory, actualGeometryFactory);

      final int axisCount = expectedGeometry.getAxisCount();
      Assert.assertEquals("Axis Count", axisCount, actualGeometry.getAxisCount());

      if (!actualGeometry.equals(axisCount, expectedGeometry)) {
        // Allow for conversion of multi part to single part
        if (expectedGeometry.getGeometryCount() != 1
          || !actualGeometry.equals(axisCount, expectedGeometry.getGeometry(0))) {
          TestUtil.failNotEquals("Geometry Equal Exact", expectedGeometry, actualGeometry);
        }
      }
    }
  }

  private static void doGeometryReadTest(final FileSystemResource resource,
    final ArrayRecord record) {
    if (GeometryReader.isReadable(resource)) {
      try (
        Reader<Geometry> geometryReader = GeometryReader.newGeometryReader(resource)) {
        final List<Geometry> geometries = geometryReader.toList();
        Assert.assertEquals("Geometry Count", 1, geometries.size());

        final Geometry expectedGeometry = record.getGeometry();
        final Geometry actualGeometry = geometries.get(0);
        assertGeometry(expectedGeometry, actualGeometry);
      }
    } else {
      Logs.debug(RecordIoTestSuite.class,
        "Reading geometry not supported for: " + resource.getFileNameExtension());
    }
  }

  private static void doRecordReadTest(final FileSystemResource resource,
    final ArrayRecord record) {
    if (RecordReader.isReadable(resource)) {
      final RecordReaderFactory recordReaderFactory = IoFactory.factory(RecordReaderFactory.class,
        resource);
      try (
        RecordReader geometryReader = RecordReader.newRecordReader(resource)) {
        final List<Record> records = geometryReader.toList();
        Assert.assertEquals("Record Count", 1, records.size());

        final Record actualRecord = records.get(0);

        if (recordReaderFactory.isCustomFieldsSupported()) {
          for (final String fieldName : record.getRecordDefinition().getFieldNames()) {
            if (!fieldName.equals("GEOMETRY")) {
              final Object expectedValue = record.getValue(fieldName);
              final Object actualValue = actualRecord.getValue(fieldName);
              final boolean equals = DataType.equal(expectedValue, actualValue);
              com.revolsys.geometry.util.Assert.equals(fieldName, equals, expectedValue,
                actualValue);
            }
          }
        }
        if (recordReaderFactory.isGeometrySupported()) {
          final Geometry expectedGeometry = record.getGeometry();
          final Geometry actualGeometry = actualRecord.getGeometry();
          assertGeometry(expectedGeometry, actualGeometry);
        }
      }
    } else {
      Logs.debug(RecordIoTestSuite.class,
        "Reading geometry not supported for: " + resource.getFileNameExtension());
    }
  }

  private static void doRecordWriteTest(final FileSystemResource resource, final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
    try (
      Writer<Record> writer = RecordWriter.newRecordWriter(record, resource)) {
      writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        final DataType geometryDataType = geometryField.getDataType();
        writer.setProperty(IoConstants.GEOMETRY_TYPE, geometryDataType);
      }
      writer.write(record);
    }
  }
}
