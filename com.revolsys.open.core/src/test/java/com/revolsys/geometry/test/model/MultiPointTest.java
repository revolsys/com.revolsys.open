package com.revolsys.geometry.test.model;

import org.junit.Test;

public class MultiPointTest {

  @Test
  public void testFromFile() {
    TestUtil.doTestGeometry(getClass(), "MultiPoint.csv");
  }
}
