package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class KmlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("KML Geometry");
    IoTestSuite.addWriteReadTestSuites(suite, "kml");
    return suite;
  }
}
