package com.revolsys.core.test.geometry.test.model.operation;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;

public class ClipRectangleTest {

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.DEFAULT_2D;

  private void assertClip(final String message, final Polygon geometry, final double minX,
    final double minY, final double maxX, final double maxY, final Geometry expected) {
    final Geometry actual = geometry.clipRectangle(minX, minY, maxX, maxY);
    Assert.assertEquals(message, expected, actual);
  }

  @Test
  public void testPolygon() {
    final Polygon polygonEmpty = GEOMETRY_FACTORY.polygon();
    final Polygon polygonM = GEOMETRY_FACTORY.polygon(2, 0, 0, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0, 4, 0,
      4, 4, 3, 4, 2, 3, 1, 4, 0, 4, 0, 0);

    assertClip("Same", polygonM, 0, 0, 4, 4, polygonM);
    assertClip("Larger", polygonM, -1, -1, 5, 5, polygonM);
    assertClip("Disjoint", polygonM, -2, -2, -1, -1, polygonEmpty);

    final Polygon polygonClipTop = GEOMETRY_FACTORY.polygon(2, 0, 0, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0,
      4, 0, 4, 3, 2, 3, 0, 3, 0, 0);
    assertClip("Top", polygonM, 0, 0, 4, 3, polygonClipTop);

    final Polygon polygonClipBottom = GEOMETRY_FACTORY.polygon(2, 1, 1, 1, 3, 2, 2, 3, 3, 3, 1, 4,
      1, 4, 4, 3, 4, 2, 3, 1, 4, 0, 4, 0, 1, 1, 1);
    assertClip("Bottom", polygonM, 0, 1, 4, 4, polygonClipBottom);

    final Polygon polygonClipLeft = GEOMETRY_FACTORY.polygon(2, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0, 4, 0,
      4, 4, 3, 4, 2, 3, 1, 4, 1, 0);
    assertClip("Left", polygonM, 1, 0, 4, 4, polygonClipLeft);

    final Polygon polygonClipRight = GEOMETRY_FACTORY.polygon(2, 0, 0, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0,
      3, 4, 2, 3, 1, 4, 0, 4, 0, 0);
    assertClip("Right", polygonM, 0, 0, 3, 4, polygonClipRight);

    final Polygon polygonClipAll = GEOMETRY_FACTORY.polygon(2, 1, 1, 1, 3, 2, 2, 3, 3, 3, 1, 3, 3,
      2, 3, 1, 3, 1, 1);
    assertClip("All", polygonM, 1, 1, 3, 3, polygonClipAll);

    final Polygon hexagon = GEOMETRY_FACTORY.polygon(2, 0, 2, 2, 0, 4, 0, 6, 2, 6, 4, 4, 6, 2, 6, 0,
      4, 0, 2);

    final Polygon polygonClipContained = GEOMETRY_FACTORY.polygon(2, 1, 1, 5, 1, 5, 5, 1, 5, 1, 1);
    assertClip("Hexagon Contained", hexagon, 1, 1, 5, 5, polygonClipContained);

    final Polygon hexagonLeftRight = GEOMETRY_FACTORY.polygon(2, 1, 1, 2, 0, 4, 0, 5, 1, 5, 5, 4, 6,
      2, 6, 1, 5, 1, 1);
    assertClip("Hexagon Left/Right", hexagon, 1, 0, 5, 6, hexagonLeftRight);

    final Polygon polygonClipTopBottom = GEOMETRY_FACTORY.polygon(2, 0, 2, 1, 1, 5, 1, 6, 2, 6, 4,
      5, 5, 1, 5, 0, 4, 0, 2);
    assertClip("Hexagon Top/Bottom", hexagon, 0, 1, 6, 5, polygonClipTopBottom);
  }
}
