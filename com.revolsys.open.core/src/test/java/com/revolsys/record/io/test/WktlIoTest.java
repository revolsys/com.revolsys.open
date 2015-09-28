package com.revolsys.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WktlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("WKT Geometry");
    RecordIoTestSuite.addWriteReadTest(suite, "WKT", "wkt");
    return suite;
  }
}
