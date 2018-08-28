package com.revolsys.core.test.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.PointEditor;

public class PointEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  @Test
  public void testPointEditorNotModified() {
    final Point point = WGS84_2D.point(100.0, 200.0);
    final PointEditor pointEditor = point.newGeometryEditor();
    pointEditor.setZ(13);
    final Point newPoint = pointEditor.newGeometry();
    Assert.assertSame(point, newPoint);

  }

  @Test
  public void testPointEditorSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final Point point = WGS84_2D.point(100.0, 200.0);
      final PointEditor pointEditor = point.newGeometryEditor(4);
      final int newValue = i * 10;
      pointEditor.setCoordinate(i, newValue);
      final Point newPoint = pointEditor.newGeometry();
      Assert.assertNotSame(point, newPoint);
      Assert.assertEquals(newValue, newPoint.getCoordinate(i), 0.0);
    }
  }

  @Test
  public void testPointEditorSetM() {
    final Point point = WGS84_2D.point(100.0, 200.0);
    final PointEditor pointEditor = point.newGeometryEditor(4);
    pointEditor.setM(10);
    final Point newPoint = pointEditor.newGeometry();
    Assert.assertNotSame(point, newPoint);
    Assert.assertEquals(10.0, newPoint.getM(), 0.0);
  }

  @Test
  public void testPointEditorSetX() {
    final Point point = WGS84_2D.point(100.0, 200.0);
    final PointEditor pointEditor = point.newGeometryEditor(3);
    pointEditor.setX(10);
    final Point newPoint = pointEditor.newGeometry();
    Assert.assertNotSame(point, newPoint);
    Assert.assertEquals(10.0, newPoint.getX(), 0.0);
  }

  @Test
  public void testPointEditorSetY() {
    final Point point = WGS84_2D.point(100.0, 200.0);
    final PointEditor pointEditor = point.newGeometryEditor(3);
    pointEditor.setY(10);
    final Point newPoint = pointEditor.newGeometry();
    Assert.assertNotSame(point, newPoint);
    Assert.assertEquals(10.0, newPoint.getY(), 0.0);
  }

  @Test
  public void testPointEditorSetZ() {
    final Point point = WGS84_2D.point(100.0, 200.0);
    final PointEditor pointEditor = point.newGeometryEditor(3);
    pointEditor.setZ(10);
    final Point newPoint = pointEditor.newGeometry();
    Assert.assertNotSame(point, newPoint);
    Assert.assertEquals(10.0, newPoint.getZ(), 0.0);
  }
}
