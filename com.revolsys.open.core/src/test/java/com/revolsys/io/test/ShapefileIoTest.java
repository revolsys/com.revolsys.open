package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ShapefileIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Shapefile Geometry");
    IoTestSuite.addWriteReadTestSuites(suite, "shp");
    return suite;
  }
}
