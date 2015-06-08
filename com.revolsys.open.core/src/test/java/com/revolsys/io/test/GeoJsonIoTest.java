package com.revolsys.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class GeoJsonIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("GeoJson Geometry");
    IoTestSuite.addGeometryTestSuites(suite, "GeoJson", IoTestSuite.class, "doWriteReadTest",
      "geojson");
    return suite;
  }
}
