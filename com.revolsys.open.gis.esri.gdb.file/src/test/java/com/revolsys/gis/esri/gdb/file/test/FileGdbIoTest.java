package com.revolsys.gis.esri.gdb.file.test;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.Assert;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStoreFactory;
import com.revolsys.gis.util.NoOp;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.test.IoTestSuite;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.test.geometry.TestUtil;

public class FileGdbIoTest {

  public static void doWriteReadTest(GeometryFactory geometryFactory,
    final DataType dataType, Geometry geometry) {
    if (geometry.isEmpty() || geometryFactory.getAxisCount() == 4) {
      return;
    }
    geometryFactory = GeometryFactory.getFactory(geometryFactory.getSrid(),
      geometryFactory.getAxisCount(), 10000000, 10000000);

    geometry = geometry.convert(geometryFactory);
    final String geometryTypeString = dataType.toString();
    String name = "/tmp/revolsystest/io/gdb/" + geometryTypeString + "_"
      + geometryFactory.getAxisCount();
    if (geometry instanceof GeometryCollection) {
      name += "_" + geometry.getGeometryCount();
    }
    if (geometry instanceof MultiPolygon) {
      NoOp.noOp();
    }
    if (geometry instanceof Polygonal) {
      name += "_" + geometry.getGeometryComponents(LinearRing.class).size();
    }
    final File file = new File(name + "_" + geometry.getVertexCount() + ".gdb");
    FileUtil.deleteDirectory(file);
    file.getParentFile().mkdirs();
    try (
      final CapiFileGdbDataObjectStore dataStore = FileGdbDataObjectStoreFactory.create(file)) {
      dataStore.setCreateMissingTables(true);
      dataStore.setCreateMissingDataStore(true);
      dataStore.initialize();

      final String typePath = "/" + geometryTypeString;
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
        typePath);
      metaData.addAttribute("ID", DataTypes.INT, true);
      metaData.addAttribute("GEOMETRY", dataType, true);
      metaData.setGeometryFactory(geometryFactory);
      dataStore.getMetaData(metaData);
      try (
        Writer<DataObject> writer = dataStore.createWriter()) {
        writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        writer.setProperty(IoConstants.GEOMETRY_TYPE, dataType);

        final DataObject record = dataStore.create(typePath);
        record.setValue("ID", 1);
        record.setGeometryValue(geometry);
        writer.write(record);
      }
      try (
        Reader<DataObject> reader = dataStore.query(typePath)) {
        final List<DataObject> objects = reader.read();
        Assert.assertEquals("Geometry Count", 1, objects.size());
        final Geometry actual = objects.get(0).getGeometryValue();
        Assert.assertEquals("Empty", geometry.isEmpty(), actual.isEmpty());
        if (!geometry.isEmpty()) {
          Assert.assertEquals("Axis Count", geometry.getAxisCount(),
            actual.getAxisCount());
          if (!actual.equalsExact(geometry)) {
            // Allow for conversion of multi part to single part
            if (geometry.getGeometryCount() != 1
              || !actual.equalsExact(geometry.getGeometry(0))) {
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
