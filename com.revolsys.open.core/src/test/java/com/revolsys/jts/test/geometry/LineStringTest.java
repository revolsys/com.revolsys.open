package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class LineStringTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "LineString.csv");
  }
}
