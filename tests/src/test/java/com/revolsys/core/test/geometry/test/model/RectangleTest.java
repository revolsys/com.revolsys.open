package com.revolsys.core.test.geometry.test.model;

import org.junit.Test;

import com.revolsys.core.test.util.TestUtil;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.testapi.GeometryAssertUtil;
import com.revolsys.util.Debug;

public class RectangleTest extends GeometryAssertUtil {

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed2d(1000, 1000);

  private static void assertInsertion(final String message, final Geometry geometry,
    final double minX, final double minY, final double maxX, final double maxY,
    final Geometry expected) {
    final RectangleXY rectangle = GEOMETRY_FACTORY.newRectangleCorners(minX, minY, maxX, maxY);

    final Geometry actualIntersectsRectangle = geometry.intersectionRectangle(rectangle);
    assertEqualsGeometry(message, expected, actualIntersectsRectangle);

    final Geometry actualIntersectsGeometry = rectangle.intersection(geometry);
    assertEqualsGeometry(message, expected, actualIntersectsGeometry);
  }

  private void assertClip(final String message, final Polygon geometry, final double minX,
    final double minY, final double maxX, final double maxY, final Geometry expected) {
    final RectangleXY rectangle = GEOMETRY_FACTORY.newRectangleCorners(minX, minY, maxX, maxY);

    final Geometry actualIntersectsRectangle = geometry.intersectionRectangle(rectangle);
    assertEqualsGeometry(message, expected, actualIntersectsRectangle);

    final Geometry actualIntersectsGeometry = rectangle.intersection(geometry);
    assertEqualsGeometry(message, expected, actualIntersectsGeometry);
  }

  private Geometry getGeometry(final Record record, final String fieldName) {
    final String wkt = record.getString(fieldName);
    return GEOMETRY_FACTORY.geometry(wkt);
  }

  @Test
  public void testIntersection() {
    TestUtil.enableInfo(getClass());
    try (
      RecordReader reader = RecordReader.newRecordReader(
        new ClassPathResource("Rectangle-Intersection.tsv", RectangleTest.class))) {
      for (final Record record : reader) {
        final String name = record.getString("Name");
        final String type = record.getString("Type");
        final String rectangle = record.getString("Rectangle");
        final Geometry geometry = getGeometry(record, "Geometry");

        Geometry expected = getGeometry(record, "Geometry_Expected");
        if (expected == null) {
          expected = geometry;
        }
        final String[] rectangleParts = rectangle.split(",");
        final double minX = Double.valueOf(rectangleParts[0]);
        final double minY = Double.valueOf(rectangleParts[1]);
        final double maxX = Double.valueOf(rectangleParts[2]);
        final double maxY = Double.valueOf(rectangleParts[3]);
        TestUtil.logValues(this, record);
        if ("centre out left, out bottom".equals(name)) {
          Debug.noOp();
        }
        assertInsertion(type + "_" + name, geometry, minX, minY, maxX, maxY, expected);
        if (geometry instanceof LineString) {
          final Geometry geometryReverse = geometry.reverse();
          final Geometry expectedReverse = expected.reverse();
          assertInsertion(type + "_" + name, geometryReverse, minX, minY, maxX, maxY,
            expectedReverse);
        }
      }
    }
  }

  @Test
  public void testPolygonIntersection() {
    final Polygon polygonEmpty = GEOMETRY_FACTORY.polygon();
    final Polygon polygonM = GEOMETRY_FACTORY.polygon(2, 0, 0, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0, 4, 0,
      4, 4, 3, 4, 2, 3, 1, 4, 0, 4, 0, 0);

    assertClip("Same", polygonM, 0, 0, 4, 4, polygonM);
    assertClip("Larger", polygonM, -1, -1, 5, 5, polygonM);
    assertClip("Disjoint", polygonM, -2, -2, -1, -1, polygonEmpty);

    final Polygonal polygonClipTop = GEOMETRY_FACTORY.geometry(
      "MULTIPOLYGON(((1 0,1 3,0 3,0 0,1 0)),((3 3,2 3,1 3,2 2,3 3)),((3 0,4 0,4 3,3 3,3 0)))");
    assertClip("Top", polygonM, 0, 0, 4, 3, polygonClipTop);

    final Polygon polygonClipBottom = GEOMETRY_FACTORY.polygon(2, 1, 1, 1, 3, 2, 2, 3, 3, 3, 1, 4,
      1, 4, 4, 3, 4, 2, 3, 1, 4, 0, 4, 0, 1, 1, 1);
    assertClip("Bottom", polygonM, 0, 1, 4, 4, polygonClipBottom);

    // final Polygon polygonClipLeft = GEOMETRY_FACTORY.polygon(2, 1, 0, 1, 3, 2, 2, 3, 3, 3, 0, 4,
    // 0,
    // 4, 4, 3, 4, 2, 3, 1, 4, 1, 0);
    // assertClip("Left", polygonM, 1, 0, 4, 4, polygonClipLeft);

    // final Polygon polygonClipRight = GEOMETRY_FACTORY.polygon(2, 0, 0, 1, 0, 1, 3, 2, 2, 3, 3, 3,
    // 0,
    // 3, 4, 2, 3, 1, 4, 0, 4, 0, 0);
    // assertClip("Right", polygonM, 0, 0, 3, 4, polygonClipRight);

    // final Polygon polygonClipAll = GEOMETRY_FACTORY.polygon(2, 1, 1, 1, 3, 2, 2, 3, 3, 3, 1, 3,
    // 3,
    // 2, 3, 1, 3, 1, 1);
    // assertClip("All", polygonM, 1, 1, 3, 3, polygonClipAll);

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
