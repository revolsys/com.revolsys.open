package com.revolsys.jts.test.geometry;

import org.junit.Test;

public class MultiPolygonTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "MultiPolygon.csv");
  }
}
