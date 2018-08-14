package com.revolsys.core.test.geometry.test.model;

import org.junit.Test;

import com.revolsys.testapi.GeometryAssertUtil;

public class MultiPolygonTest {

  @Test
  public void testFromFile() {
    GeometryAssertUtil.doTestGeometry(getClass(), "MultiPolygon.csv");
  }
}
