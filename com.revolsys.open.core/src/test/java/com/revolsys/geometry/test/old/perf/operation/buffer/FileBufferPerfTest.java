package com.revolsys.geometry.test.old.perf.operation.buffer;

import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.util.Stopwatch;

public class FileBufferPerfTest {
  private static final GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);

  static final int MAX_ITER = 1;

  public static void main(final String[] args) {
    final FileBufferPerfTest test = new FileBufferPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  GeometryFactory factory = GeometryFactory.DEFAULT_3D;

  boolean testFailed = false;

  public FileBufferPerfTest() {
  }

  void runAll(final List polys, final double distance) {
    // System.out.println("Geom count = " + polys.size() + " distance = "
    // + distance);
    final Stopwatch sw = new Stopwatch();
    for (final Iterator i = polys.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      g.buffer(distance);
      System.out.print(".");
    }
    // System.out.println();
    // System.out.println(" Time = " + sw.getTimeString());
  }

  public void test() throws Exception {
    test("africa.wkt");
  }

  public void test(final String file) throws Exception {
    final List polys = InteriorPointTest.getTestGeometries(file);

    runAll(polys, 0.01);
    runAll(polys, 0.1);
    runAll(polys, 1.0);
    runAll(polys, 10.0);
    runAll(polys, 100.0);
    runAll(polys, 1000.0);
  }
}
