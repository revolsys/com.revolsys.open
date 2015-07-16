package com.revolsys.jts.testold.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.operation.union.CascadedPolygonUnion;
import com.revolsys.jts.testold.junit.GeometryUtils;

import junit.framework.TestCase;
import junit.framework.TestCase;

/**
 * Large-scale tests of {@link CascadedPolygonUnion}
 * using synthetic datasets.
 *
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTest extends TestCase {
  private static CascadedPolygonUnionTester tester = new CascadedPolygonUnionTester();

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(CascadedPolygonUnionTest.class);
  }

  GeometryFactory geomFact = GeometryFactory.floating3();

  public CascadedPolygonUnionTest(final String name) {
    super(name);
  }

  private Collection createDiscs(final int num, final double radius) {
    final List geoms = new ArrayList();
    for (int i = 0; i < num; i++) {
      for (int j = 0; j < num; j++) {
        final Point pt = new PointDouble((double)i, j, Point.NULL_ORDINATE);
        final Geometry ptGeom = this.geomFact.point(pt);
        final Geometry disc = ptGeom.buffer(radius);
        geoms.add(disc);
      }
    }
    return geoms;
  }

  private void runTest(final Collection geoms, final double minimumMeasure) {
    assertTrue(tester.test(geoms, minimumMeasure));
  }

  // TODO: add some synthetic tests

  public void testBoxes() throws Exception {
    runTest(GeometryUtils.readWKT(new String[] {
      "POLYGON ((80 260, 200 260, 200 30, 80 30, 80 260))",
      "POLYGON ((30 180, 300 180, 300 110, 30 110, 30 180))",
      "POLYGON ((30 280, 30 150, 140 150, 140 280, 30 280))"
    }), CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs1() throws Exception {
    final Collection geoms = createDiscs(5, 0.7);

    // System.out.println(this.geomFact.buildGeometry(geoms));

    runTest(geoms, CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs2() throws Exception {
    final Collection geoms = createDiscs(5, 0.55);

    // System.out.println(this.geomFact.buildGeometry(geoms));

    runTest(geoms, CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }
}
