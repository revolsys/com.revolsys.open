package com.revolsys.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class KmlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("KML Geometry");
    RecordIoTestSuite.addWriteReadTest(suite, "KML", "kml");
    RecordIoTestSuite.addWriteReadTest(suite, "KMZ", "kmz");
    return suite;
  }
}
