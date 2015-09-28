package com.revolsys.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class GeoJsonIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("GeoJson Geometry");
    RecordIoTestSuite.addWriteReadTest(suite, "GeoJson", "geojson");
    return suite;
  }
}
