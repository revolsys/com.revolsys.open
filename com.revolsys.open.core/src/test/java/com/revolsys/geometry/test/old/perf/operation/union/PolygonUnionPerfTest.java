package com.revolsys.geometry.test.old.perf.operation.union;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.util.GeometricShapeFactory;
import com.revolsys.geometry.wkb.WKTReader;

public class PolygonUnionPerfTest {

  private static final GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);

  static final int MAX_ITER = 1;

  static WKTReader wktRdr = new WKTReader(geometryFactory);

  public static void main(final String[] args) {
    final PolygonUnionPerfTest test = new PolygonUnionPerfTest();

    // test.test();
    test.testRampItems();

  }

  GeometryFactory factory = GeometryFactory.DEFAULT_3D;

  boolean testFailed = false;

  public PolygonUnionPerfTest() {
  }

  Geometry newPolygon(final Point base, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory(this.factory);
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);

    final Geometry poly = gsf.newCircle();
    // Geometry poly = gsf.createRectangle();

    // System.out.println(circle);
    return poly;
  }

  /**
   * Creates a grid of circles with a small percentage of overlap
   * in both directions.
   * This approximated likely real-world cases well,
   * and seems to produce
   * close to worst-case performance for the Iterated algorithm.
   *
   * Sample times:
   * 1000 items/100 pts - Cascaded: 2718 ms, Iterated 150 s
   *
   * @param nItems
   * @param size
   * @param nPts
   * @return
   */
  List newPolygons(final int nItems, final double size, final int nPts) {

    // between 0 and 1
    final double overlapPct = 0.2;

    final int nCells = (int)Math.sqrt(nItems);

    final List geoms = new ArrayList();
    // double width = env.getWidth();
    final double width = nCells * (1 - overlapPct) * size;

    // this results in many final polys
    final double height = nCells * 2 * size;

    // this results in a single final polygon
    // double height = width;

    final double xInc = width / nCells;
    final double yInc = height / nCells;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        final Point base = new PointDouble(i * xInc, j * yInc);
        final Geometry poly = newPolygon(base, size, nPts);
        geoms.add(poly);
        // System.out.println(poly);
      }
    }
    return geoms;
  }

  public void test() {
    // test(5, 100, 10.0);
    test(1000, 100, 10.0);
  }

  public void test(final int nItems, final int nPts, final double size) {
    // System.out.println("---------------------------------------------------------");
    // System.out.println("# pts/item: " + nPts);

    final List polys = newPolygons(nItems, size, nPts);

    // System.out.println();
    // System.out.println("Running with " + nPts + " points");

    final UnionPerfTester tester = new UnionPerfTester(polys);
    tester.runAll();
  }

  public void testRampItems() {
    final int nPts = 1000;

    test(5, nPts, 10.0);
    test(5, nPts, 10.0);
    test(25, nPts, 10.0);
    test(50, nPts, 10.0);
    test(100, nPts, 10.0);
    test(200, nPts, 10.0);
    test(400, nPts, 10.0);
    test(500, nPts, 10.0);
    test(1000, nPts, 10.0);
    test(2000, nPts, 10.0);
    test(4000, nPts, 10.0);
  }

}
