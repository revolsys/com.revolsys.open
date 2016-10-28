package com.revolsys.geometry.test.old.perf.triangulate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.triangulate.DelaunayTriangulationBuilder;
import com.revolsys.geometry.util.Stopwatch;

/**
 * Test robustness of Delaunay computation.
 * Test dataset is constructed to have many points
 * with a large base offset.  This reduces
 * the precision available for the arithmetic in the inCircle test.
 * This causes incorrect values to be computed by using
 * the simple double-precision approach.
 *
 * @author Martin Davis
 *
 */
public class DelaunayRobustTest {
  final static double BASE_OFFSET = 1.0e7;

  final static GeometryFactory geomFact = GeometryFactory.DEFAULT;

  final static double SIDE_LEN = 1.0;

  public static void main(final String args[]) {
    final DelaunayRobustTest test = new DelaunayRobustTest();
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

  List randomPointsInGrid(final int nPts, final double basex, final double basey) {
    final List pts = new ArrayList();

    final int nSide = (int)Math.sqrt(nPts) + 1;

    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        final double x = basex + i * SIDE_LEN + SIDE_LEN * Math.random();
        final double y = basey + j * SIDE_LEN + SIDE_LEN * Math.random();
        pts.add(new PointDoubleXY(x, y));
      }
    }
    return pts;
  }

  public void run() {
    run(100000);
  }

  public void run(final int nPts) {
    // System.out.println("Base offset: " + BASE_OFFSET);

    final List pts = randomPointsInGrid(nPts, BASE_OFFSET, BASE_OFFSET);
    // System.out.println("# pts: " + pts.size());
    final Stopwatch sw = new Stopwatch();
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(pts);

    // Geometry g = builder.getEdges(geomFact);
    // don't actually form output geometry, to save time and memory
    builder.getSubdivision();

    // System.out.println(" -- Time: " + sw.getTimeString() + " Mem: "
    // + Memory.usedTotalString());
    // System.out.println(g);
  }
}
