package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class PointTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "Point.csv");
  }
}
