package com.revolsys.core.test.geometry.test.old.perf.operation.union;

import java.util.List;

import com.revolsys.core.test.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.model.GeometryFactory;

public class FileUnionPerfTest {
  private static final GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  static final int MAX_ITER = 1;

  public static void main(final String[] args) {
    final FileUnionPerfTest test = new FileUnionPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  GeometryFactory factory = GeometryFactory.DEFAULT_3D;

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
