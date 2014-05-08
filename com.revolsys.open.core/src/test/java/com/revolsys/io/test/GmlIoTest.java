package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class GmlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("GML Geometry");
    IoTestSuite.addGeometryTestSuites(suite, "GML", IoTestSuite.class,
      "doWriteReadTest", "gml");
    return suite;
  }
}
