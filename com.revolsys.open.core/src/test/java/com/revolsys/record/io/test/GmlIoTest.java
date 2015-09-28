package com.revolsys.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class GmlIoTest {

  public static Test suite() {
    final TestSuite suite = new TestSuite("GML Geometry");
    RecordIoTestSuite.addWriteReadTest(suite, "GML", "gml");
    return suite;
  }
}
