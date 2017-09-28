package com.revolsys.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.editor.PolygonEditor;

public class PolygonEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final Polygon POLYGON = WGS84_2D.polygon(
    WGS84_2D.linearRing(2, 0.0, 0.0, 0, 10, 10, 10, 10, 0, 0, 0), //
    WGS84_2D.linearRing(2, 2.0, 2.0, 2, 8, 8, 8, 8, 2, 2, 2));

  @Test
  public void testNotModified() {
    final Polygon polygon = POLYGON;
    final PolygonEditor polygonEditor = polygon.newGeometryEditor();
    try {
      polygonEditor.setZ(new int[0], 13);
      Assert.fail("Invalid index should cause exception");
    } catch (final Exception e) {
    }
  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final Polygon polygon = POLYGON;
      final PolygonEditor polygonEditor = polygon.newGeometryEditor(4);
      final double newValue = POLYGON.getCoordinate(1, 1, i);
      polygonEditor.setCoordinate(1, 1, i, newValue);
      final Polygon newPolygon = polygonEditor.newGeometry();
      Assert.assertNotSame(polygon, newPolygon);
      Assert.assertEquals(newValue, newPolygon.getCoordinate(1, 1, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final Polygon polygon = POLYGON;
    final PolygonEditor polygonEditor = polygon.newGeometryEditor(4);
    polygonEditor.setM(1, 1, 10);
    final Polygon newPolygon = polygonEditor.newGeometry();
    Assert.assertNotSame(polygon, newPolygon);
    Assert.assertEquals(10.0, newPolygon.getM(1, 1), 0.0);
  }

  @Test
  public void testSetX() {
    final Polygon polygon = POLYGON;
    final PolygonEditor polygonEditor = polygon.newGeometryEditor(3);
    polygonEditor.setX(1, 1, 10);
    final Polygon newPolygon = polygonEditor.newGeometry();
    Assert.assertNotSame(polygon, newPolygon);
    Assert.assertEquals(10.0, newPolygon.getX(1, 1), 0.0);
  }

  @Test
  public void testSetY() {
    final Polygon polygon = POLYGON;
    final PolygonEditor polygonEditor = polygon.newGeometryEditor(3);
    polygonEditor.setY(1, 1, 10);
    final Polygon newPolygon = polygonEditor.newGeometry();
    Assert.assertNotSame(polygon, newPolygon);
    Assert.assertEquals(10.0, newPolygon.getY(1, 1), 0.0);
  }

  @Test
  public void testSetZ() {
    final Polygon polygon = POLYGON;
    final PolygonEditor polygonEditor = polygon.newGeometryEditor(3);
    polygonEditor.setZ(1, 1, 10);
    final Polygon newPolygon = polygonEditor.newGeometry();
    Assert.assertNotSame(polygon, newPolygon);
    Assert.assertEquals(10.0, newPolygon.getZ(1, 1), 0.0);
  }
}
