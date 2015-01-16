package com.revolsys.jts.testold.perf.operation.union;

import java.util.List;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.testold.algorithm.InteriorPointTest;

public class FileUnionPerfTest {
  public static void main(final String[] args) {
    final FileUnionPerfTest test = new FileUnionPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  static final int MAX_ITER = 1;

  private static final GeometryFactory geometryFactory = GeometryFactory.floating(
    0, 2);

  static WKTReader wktRdr = new WKTReader(geometryFactory);

  GeometryFactory factory = GeometryFactory.floating3();

  boolean testFailed = false;

  public FileUnionPerfTest() {
  }

  public void test() throws Exception {
    test("africa.wkt");
  }

  public void test(final String file) throws Exception {
    final List polys = InteriorPointTest.getTestGeometries(file);

    final UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }

}
