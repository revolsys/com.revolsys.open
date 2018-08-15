package com.revolsys.core.test.geometry.test.model;

import org.junit.Test;

import com.revolsys.testapi.GeometryAssert;

public class MultiLineStringTest {

  @Test
  public void testFromFile() {
    GeometryAssert.doTestGeometry(getClass(), "MultiLineString.csv");
  }
}
