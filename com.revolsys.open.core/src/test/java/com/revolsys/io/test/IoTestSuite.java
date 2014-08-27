package com.revolsys.io.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.core.io.FileSystemResource;

import com.revolsys.data.io.RecordIo;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.test.geometry.GeometryTestUtil;
import com.revolsys.jts.test.geometry.TestUtil;
import com.revolsys.junit.InvokeMethodTestCase;

@RunWith(Suite.class)
@SuiteClasses({
  ShapefileIoTest.class, GeoJsonIoTest.class, GmlIoTest.class, KmlIoTest.class,
  WktlIoTest.class
})
public class IoTestSuite {

  public static void addGeometryTestSuites(final TestSuite suite,
    final String namePrefix, final Object methodObject,
    final String methodName, final Object... extraParameters) {
    final List<DataType> geometryDataTypes = Arrays.asList(DataTypes.POINT,
      DataTypes.LINE_STRING, DataTypes.POLYGON, DataTypes.MULTI_POINT,
      DataTypes.MULTI_LINE_STRING, DataTypes.MULTI_POLYGON);
    for (final DataType dataType : geometryDataTypes) {
      final TestSuite dataTypeSuite = new TestSuite(namePrefix + " "
        + dataType.toString());
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
              final GeometryFactory geometryFactory = GeometryFactory.fixed(
                4326, axisCount, 0.0, 0.0);
              double delta = 1.0;
              if (geometryFactory.isProjected()) {
                delta = 1000.0;
              }
              final Geometry geometry = GeometryTestUtil.geometry(
                geometryFactory, dataType, geometryCount, ringCount,
                vertexCount, delta);
              String name = namePrefix + " " + dataType + " A=" + axisCount
                + " G=" + geometryCount;
              if (maxVertexCount > 2) {
                name += " V=" + vertexCount;
              }
              if (maxRingCount > 2) {
                name += " R=" + ringCount;
              }
              final List<Object> parameters = new ArrayList<>();
              parameters.add(geometryFactory);
              parameters.add(dataType);
              parameters.add(geometry);
              for (final Object parameter : extraParameters) {
                parameters.add(parameter);
              }
              final TestCase testCase = new InvokeMethodTestCase(name,
                methodObject, methodName, parameters);
              dataTypeSuite.addTest(testCase);
            }
          }
        }
      }
    }
  }

  public static void doWriteReadTest(final GeometryFactory geometryFactory,
    final DataType dataType, final Geometry geometry, final String fileExtension) {
    final String geometryTypeString = dataType.toString();
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      geometryTypeString);
    recordDefinition.addAttribute("ID", DataTypes.INT, true);
    recordDefinition.addAttribute("GEOMETRY", dataType, true);
    recordDefinition.setGeometryFactory(geometryFactory);
    final File file = new File("/tmp/revolsystest/io/" + fileExtension + "/"
      + geometryTypeString + "_" + geometryFactory.getAxisCount() + "_"
      + geometry.getVertexCount() + "." + fileExtension);
    file.delete();
    file.getParentFile().mkdirs();
    final FileSystemResource resource = new FileSystemResource(file);
    try (
      Writer<Record> writer = RecordIo.recordWriter(
        recordDefinition, resource)) {
      writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
      writer.setProperty(IoConstants.GEOMETRY_TYPE, dataType);

      final ArrayRecord record = new ArrayRecord(recordDefinition);
      record.setValue("ID", 1);
      record.setGeometryValue(geometry);
      writer.write(record);
    }
    try (
      Reader<Geometry> reader = AbstractGeometryReaderFactory.geometryReader(resource)) {
      reader.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
      final List<Geometry> geometries = reader.read();
      Assert.assertEquals("Geometry Count", 1, geometries.size());
      final Geometry actual = geometries.get(0);
      Assert.assertEquals("Empty", geometry.isEmpty(), actual.isEmpty());
      if (!geometry.isEmpty()) {
        final int axisCount = geometry.getAxisCount();
        Assert.assertEquals("Axis Count", axisCount, actual.getAxisCount());
        if (!actual.equals(axisCount, geometry)) {
          // Allow for conversion of multi part to single part
          if (geometry.getGeometryCount() != 1
            || !actual.equals(axisCount, geometry.getGeometry(0))) {
            TestUtil.failNotEquals("Geometry Equal Exact", geometry, actual);
          }
        }
      }
    }
  }

}
