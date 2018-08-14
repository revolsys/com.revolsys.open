package com.revolsys.core.test.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ShapefileIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Shapefile Geometry");
    RecordIoTestSuite.addWriteReadTest(suite, "Shapefile", "shp");
    RecordIoTestSuite.addWriteReadTest(suite, "Shapefile Zip", "shpz");
    return suite;
  }
}
