package com.revolsys.geometry.test.old.geom;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;

/**
 * Test spatial predicate optimizations for rectangles.
 *
 * @version 1.7
 */

public class RectanglePredicateTest {
  GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private void runRectanglePred(final Geometry rect, final Geometry testGeom) {
    final boolean intersectsValue = rect.intersects(testGeom);
    final boolean relateIntersectsValue = rect.relate(testGeom).isIntersects();
    final boolean intersectsOK = intersectsValue == relateIntersectsValue;

    final boolean containsValue = rect.contains(testGeom);
    final boolean relateContainsValue = rect.relate(testGeom).isContains();
    final boolean containsOK = containsValue == relateContainsValue;

    Assert.assertTrue("intersects", intersectsOK);
    Assert.assertTrue("contains", containsOK);
  }

  @Test
  public void testAngleOnBoundary() throws Exception {
    final Polygon polygon = this.geometryFactory.polygon(2, 10.0, 10, 30, 10, 30, 30, 10, 30, 10,
      10);
    final LineString line = this.geometryFactory.lineString(2, 10.0, 30, 10, 10, 30, 10);
    runRectanglePred(polygon, line);
  }

  @Test
  public void testShortAngleOnBoundary() throws Exception {
    final Polygon polygon = this.geometryFactory.polygon(2, 10.0, 10, 30, 10, 30, 30, 10, 30, 10,
      10);
    final LineString line = this.geometryFactory.lineString(2, 10.0, 25, 10, 10, 25, 10);
    runRectanglePred(polygon, line);
  }

}
