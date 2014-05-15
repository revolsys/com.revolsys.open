package com.revolsys.jts.testold.perf.operation.union;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.union.CascadedPolygonUnion;
import com.revolsys.jts.util.Stopwatch;

public class UnionPerfTester {
  public static final int CASCADED = 1;

  public static final int ITERATED = 2;

  public static final int BUFFER0 = 3;

  public static final int ORDERED = 4;

  static final int MAX_ITER = 1;

  private static final GeometryFactory geometryFactory = GeometryFactory.floating(
    0, 2);

  static WKTReader wktRdr = new WKTReader(geometryFactory);

  public static void run(final String testName, final int testType,
    final List polys) {
    final UnionPerfTester test = new UnionPerfTester(polys);
    test.run(testName, testType);
  }

  public static void runAll(final List polys) {
    final UnionPerfTester test = new UnionPerfTester(polys);
    test.runAll();
  }

  Stopwatch sw = new Stopwatch();

  GeometryFactory factory = GeometryFactory.floating3();

  private final List polys;

  public UnionPerfTester(final List polys) {
    this.polys = polys;
  }

  void printItemEnvelopes(final List tree) {
    final Envelope itemEnv = new Envelope();
    for (final Iterator i = tree.iterator(); i.hasNext();) {
      final Object o = i.next();
      if (o instanceof List) {
        printItemEnvelopes((List)o);
      } else if (o instanceof Geometry) {
        itemEnv.expandToInclude(((Geometry)o).getBoundingBox());
      }
    }
  //  System.out.println(this.factory.toGeometry(itemEnv));
  }

  public void run(final String testName, final int testType) {
  //  System.out.println();
  //  System.out.println("======= Union Algorithm: " + testName + " ===========");

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
  //  System.out.println("Finished in " + sw.getTimeString());
  }

  public void runAll() {
  //  System.out.println("# items: " + this.polys.size());
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
