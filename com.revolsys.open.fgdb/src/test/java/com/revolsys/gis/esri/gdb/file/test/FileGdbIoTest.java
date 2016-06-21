package com.revolsys.gis.esri.gdb.file.test;

import java.io.File;
import java.util.List;

import org.junit.Assert;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.test.model.TestUtil;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinitionImpl;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FileGdbIoTest {

  public static void doWriteReadTest(GeometryFactory geometryFactory, final DataType dataType,
    Geometry geometry) {
    if (geometry.isEmpty() || geometryFactory.getAxisCount() == 4) {
      return;
    }
    geometryFactory = GeometryFactory.fixed(geometryFactory.getCoordinateSystemId(),
      geometryFactory.getAxisCount(), 10000000.0, 10000000.0);

    geometry = geometry.convertGeometry(geometryFactory);
    final String geometryTypeString = dataType.toString();
    String name = "/tmp/revolsystest/io/gdb/" + geometryTypeString + "_"
      + geometryFactory.getAxisCount();
    if (geometry.isGeometryCollection()) {
      name += "_" + geometry.getGeometryCount();
    }
    if (geometry instanceof Polygonal) {
      name += "_" + geometry.getGeometryComponents(LinearRing.class).size();
    }
    final File file = new File(name + "_" + geometry.getVertexCount() + ".gdb");
    FileUtil.deleteDirectory(file);
    file.getParentFile().mkdirs();
    try (
      final FileGdbRecordStore recordStore = FileGdbRecordStoreFactory.newRecordStore(file)) {
      recordStore.setCreateMissingTables(true);
      recordStore.setCreateMissingRecordStore(true);
      recordStore.initialize();

      final PathName typePath = PathName.newPathName("/" + geometryTypeString);
      final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(typePath);
      recordDefinition.addField("ID", DataTypes.INT, true);
      recordDefinition.addField("NAME", DataTypes.STRING, 50, false);
      recordDefinition.addField("GEOMETRY", dataType, true);
      recordDefinition.setGeometryFactory(geometryFactory);
      recordStore.getRecordDefinition(recordDefinition);
      try (
        Writer<Record> writer = recordStore.newRecordWriter()) {
        writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        writer.setProperty(IoConstants.GEOMETRY_TYPE, dataType);

        final Record record = recordStore.newRecord(typePath);
        record.setValue("ID", 1);
        record.setGeometryValue(geometry);
        writer.write(record);
      }
      try (
        Reader<Record> reader = recordStore.getRecords(typePath)) {
        final List<Record> objects = reader.toList();
        Assert.assertEquals("Geometry Count", 1, objects.size());
        final Geometry actual = objects.get(0).getGeometry();
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

  public static Test suite() {
    final TestSuite suite = new TestSuite("File GDB Geometry");
    // RecordIoTestSuite.addWriteReadTest(suite, "File GDB",
    // FileGdbIoTest.class, "doWriteReadTest");
    return suite;
  }

}
