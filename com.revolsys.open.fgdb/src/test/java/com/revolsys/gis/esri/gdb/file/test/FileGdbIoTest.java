package com.revolsys.gis.esri.gdb.file.test;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.Assert;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.test.IoTestSuite;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.test.geometry.TestUtil;

public class FileGdbIoTest {

  public static void doWriteReadTest(GeometryFactory geometryFactory,
    final DataType dataType, Geometry geometry) {
    if (geometry.isEmpty() || geometryFactory.getAxisCount() == 4) {
      return;
    }
    geometryFactory = GeometryFactory.fixed(geometryFactory.getSrid(),
      geometryFactory.getAxisCount(), 10000000.0, 10000000.0);

    geometry = geometry.convert(geometryFactory);
    final String geometryTypeString = dataType.toString();
    String name = "/tmp/revolsystest/io/gdb/" + geometryTypeString + "_"
        + geometryFactory.getAxisCount();
    if (geometry instanceof GeometryCollection) {
      name += "_" + geometry.getGeometryCount();
    }
    if (geometry instanceof Polygonal) {
      name += "_" + geometry.getGeometryComponents(LinearRing.class).size();
    }
    final File file = new File(name + "_" + geometry.getVertexCount() + ".gdb");
    FileUtil.deleteDirectory(file);
    file.getParentFile().mkdirs();
    try (
        final CapiFileGdbRecordStore recordStore = FileGdbRecordStoreFactory.create(file)) {
      recordStore.setCreateMissingTables(true);
      recordStore.setCreateMissingRecordStore(true);
      recordStore.initialize();

      final String typePath = "/" + geometryTypeString;
      final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
        typePath);
      recordDefinition.addField("ID", DataTypes.INT, true);
      recordDefinition.addField("NAME", DataTypes.STRING, 50, false);
      recordDefinition.addField("GEOMETRY", dataType, true);
      recordDefinition.setGeometryFactory(geometryFactory);
      recordStore.getRecordDefinition(recordDefinition);
      try (
          Writer<Record> writer = recordStore.createWriter()) {
        writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        writer.setProperty(IoConstants.GEOMETRY_TYPE, dataType);

        final Record record = recordStore.create(typePath);
        record.setValue("ID", 1);
        record.setGeometryValue(geometry);
        writer.write(record);
      }
      try (
          Reader<Record> reader = recordStore.query(typePath)) {
        final List<Record> objects = reader.read();
        Assert.assertEquals("Geometry Count", 1, objects.size());
        final Geometry actual = objects.get(0).getGeometryValue();
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
    IoTestSuite.addGeometryTestSuites(suite, "File GDB", FileGdbIoTest.class,
        "doWriteReadTest");
    return suite;
  }

}
