package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class PolygonTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "Polygon.csv");
  }
}
