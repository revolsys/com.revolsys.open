package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WktlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("WKT Geometry");
    IoTestSuite.addWriteReadTestSuites(suite, "wkt");
    return suite;
  }
}
