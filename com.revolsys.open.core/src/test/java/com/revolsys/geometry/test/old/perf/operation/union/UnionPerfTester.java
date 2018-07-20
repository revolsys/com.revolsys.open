package com.revolsys.geometry.test.old.perf.operation.union;

import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;
import com.revolsys.geometry.util.Stopwatch;

public class UnionPerfTester {
  public static final int BUFFER0 = 3;

  public static final int CASCADED = 1;

  private static final GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  public static final int ITERATED = 2;

  static final int MAX_ITER = 1;

  public static final int ORDERED = 4;

  public static void run(final String testName, final int testType, final List polys) {
    final UnionPerfTester test = new UnionPerfTester(polys);
    test.run(testName, testType);
  }

  public static void runAll(final List polys) {
    final UnionPerfTester test = new UnionPerfTester(polys);
    test.runAll();
  }

  GeometryFactory factory = GeometryFactory.DEFAULT_3D;

  private final List polys;

  Stopwatch sw = new Stopwatch();

  public UnionPerfTester(final List polys) {
    this.polys = polys;
  }

  public void run(final String testName, final int testType) {
    // System.out.println();
    // System.out.println("======= Union Algorithm: " + testName +
    // " ===========");

    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < MAX_ITER; i++) {
      Geometry union = null;
      switch (testType) {
        case CASCADED:
          union = unionCascaded(this.polys);
        break;
        case ITERATED:
          union = unionAllSimple(this.polys);
        break;
        case BUFFER0:
          union = unionAllBuffer(this.polys);
        break;
      }

      // printFormatted(union);

    }
    // System.out.println("Finished in " + sw.getTimeString());
  }

  public void runAll() {
    // System.out.println("# items: " + this.polys.size());
    run("Cascaded", CASCADED, this.polys);
    // run("Buffer-0", BUFFER0, polys);

    run("Iterated", ITERATED, this.polys);

  }

  public Geometry unionAllBuffer(final List geoms) {

    final Geometry gColl = this.factory.buildGeometry(geoms);
    final Geometry unionAll = gColl.buffer(0.0);
    return unionAll;
  }

  public Geometry unionAllSimple(final List geoms) {
    Geometry unionAll = null;
    int count = 0;
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry geom = (Geometry)i.next();

      if (unionAll == null) {
        unionAll = geom.clone();
      } else {
        unionAll = unionAll.union(geom);
      }

      count++;
      if (count % 100 == 0) {
        System.out.print(".");
        // System.out.println("Adding geom #" + count);
      }
    }
    return unionAll;
  }

  /*
   * public Geometry unionAllOrdered(List geoms) { // return
   * OrderedUnion.union(geoms); }
   */

  public Geometry unionCascaded(final List geoms) {
    return CascadedPolygonUnion.union(geoms);
  }
}
