package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class MultiLineStringTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "MultiLineString.csv");
  }
}
