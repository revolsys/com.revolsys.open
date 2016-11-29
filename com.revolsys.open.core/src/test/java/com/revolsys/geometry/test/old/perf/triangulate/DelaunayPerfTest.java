package com.revolsys.geometry.test.old.perf.triangulate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.triangulate.DelaunayTriangulationBuilder;
import com.revolsys.geometry.util.Stopwatch;

public class DelaunayPerfTest {
  final static GeometryFactory geomFact = GeometryFactory.DEFAULT_3D;

  final static double SIDE_LEN = 10.0;

  public static void main(final String args[]) {
    final DelaunayPerfTest test = new DelaunayPerfTest();
    test.run();
  }

  List randomPoints(final int nPts) {
    final List pts = new ArrayList();

    for (int i = 0; i < nPts; i++) {
      final double x = SIDE_LEN * Math.random();
      final double y = SIDE_LEN * Math.random();
      pts.add(new PointDoubleXY(x, y));
    }
    return pts;
  }

  List randomPointsInGrid(final int nPts) {
    final List pts = new ArrayList();

    final int nSide = (int)Math.sqrt(nPts) + 1;

    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        final double x = i * SIDE_LEN + SIDE_LEN * Math.random();
        final double y = j * SIDE_LEN + SIDE_LEN * Math.random();
        pts.add(new PointDoubleXY(x, y));
      }
    }
    return pts;
  }

  public void run() {
    run(10);
    run(10);
    run(100);
    run(1000);
    run(10000);
    run(20000);
    run(30000);
    run(100000);
    run(200000);
    run(300000);
    run(1000000);
    // run(2000000);
    run(3000000);
  }

  public void run(final int nPts) {
    final List pts = randomPoints(nPts);
    // System.out.println("# pts: " + pts.size());
    final Stopwatch sw = new Stopwatch();
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.addPoints(pts);

    // Geometry g = builder.getEdges(geomFact);
    // don't actually form output geometry, to save time and memory
    builder.getSubdivision();

    // System.out.println(" -- Time: " + sw.getTimeString() + " Mem: "
    // + Memory.usedTotalString());
    // System.out.println(g);
  }
}
