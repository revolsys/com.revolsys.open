package com.revolsys.geometry.test.model;

import org.junit.Test;

public class MultiLineStringTest {

  @Test
  public void testFromFile() {
    GeometryAssertUtil.doTestGeometry(getClass(), "MultiLineString.csv");
  }
}
