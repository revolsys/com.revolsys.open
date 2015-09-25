package com.revolsys.geometry.test.old.perf.operation.union;

import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.wkb.WKTReader;

public class FileUnionPerfTest {
  private static final GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);

  static final int MAX_ITER = 1;

  static WKTReader wktRdr = new WKTReader(geometryFactory);

  public static void main(final String[] args) {
    final FileUnionPerfTest test = new FileUnionPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

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
