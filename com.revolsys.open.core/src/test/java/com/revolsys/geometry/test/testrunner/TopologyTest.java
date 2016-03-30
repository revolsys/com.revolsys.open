package com.revolsys.geometry.test.testrunner;

import java.io.File;

import com.revolsys.geometry.model.GeometryFactory;

import junit.textui.TestRunner;

public class TopologyTest {

  private static GeometryFactory geometryFactory = GeometryFactory.DEFAULT;

  private static final TestReader testReader = new TestReader();

  public static GeometryFactory getGeometryFactory() {
    return TopologyTest.geometryFactory;
  }

  public static TestReader getTestReader() {
    return testReader;
  }

  public static void main(final String[] args) throws Throwable {
    final TestRunner runner = new TestRunner();
    runner.doRun(suite());
  }

  public static void setGeometryFactory(final GeometryFactory geometryFactory) {
    TopologyTest.geometryFactory = geometryFactory;
  }

  public static junit.framework.Test suite() throws Throwable {
    // return testReader.createTestRun(new File("src/test/testxml/Quick.xml"),
    // 1);
    return new TestDirectory(null, 0, new File("src/test/testxml/"), "Topology Tests");
  }

  public TopologyTest() {
  }

}
