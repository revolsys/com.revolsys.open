package com.revolsys.core.test.geometry.test.old.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.core.test.geometry.test.old.junit.GeometryUtils;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;

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

  GeometryFactory geomFactory = GeometryFactory.DEFAULT_3D;

  public CascadedPolygonUnionTest(final String name) {
    super(name);
  }

  private Collection<Polygon> newDiscs(final int num, final double radius) {
    final List<Polygon> discs = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      for (int j = 0; j < num; j++) {
        final Geometry point = this.geomFactory.point(i, j);
        final Polygon disc = (Polygon)point.buffer(radius);
        discs.add(disc);
      }
    }
    return discs;
  }

  private void runTest(final Collection<Polygon> polygons, final double minimumMeasure) {
    assertTrue(tester.test(polygons, minimumMeasure));
  }

  // TODO: add some synthetic tests

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public void testBoxes() throws Exception {
    final List polygons = GeometryUtils.readWKT(new String[] {
      "POLYGON ((80 260, 200 260, 200 30, 80 30, 80 260))",
      "POLYGON ((30 180, 300 180, 300 110, 30 110, 30 180))",
      "POLYGON ((30 280, 30 150, 140 150, 140 280, 30 280))"
    });
    runTest(polygons, CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs1() throws Exception {
    final Collection<Polygon> geoms = newDiscs(5, 0.7);

    // System.out.println(this.geomFact.buildGeometry(geoms));

    runTest(geoms, CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }

  public void testDiscs2() throws Exception {
    final Collection<Polygon> geoms = newDiscs(5, 0.55);

    // System.out.println(this.geomFact.buildGeometry(geoms));

    runTest(geoms, CascadedPolygonUnionTester.MIN_SIMILARITY_MEAURE);
  }
}
