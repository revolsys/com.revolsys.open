package com.revolsys.geometry.test.model;

import org.junit.Test;

public class MultiPolygonTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "MultiPolygon.csv");
  }
}
