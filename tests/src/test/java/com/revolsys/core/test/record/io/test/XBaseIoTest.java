package com.revolsys.core.test.record.io.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class XBaseIoTest {
  public static Test suite() {
    final TestSuite suite = new TestSuite("XBase");
    RecordIoTestSuite.addWriteReadTest(suite, "XBase", "dbf");
    return suite;
  }
}
