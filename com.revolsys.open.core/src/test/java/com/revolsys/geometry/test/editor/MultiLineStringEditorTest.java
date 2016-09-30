package com.revolsys.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.editor.LinealEditor;

public class MultiLineStringEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final Lineal LINEAL = WGS84_2D.lineal(//
    WGS84_2D.lineString(2, 0.0, 0.0, 0, 10, 10, 10, 10, 0, 0, 0), //
    WGS84_2D.lineString(2, 2.0, 2.0, 2, 8, 8, 8, 8, 2, 2, 2) //
  );

  @Test
  public void testNotModified() {
    final LinealEditor editor = LINEAL.newGeometryEditor();
    editor.setZ(13);
    final Lineal newGeometry = editor.newGeometry();
    Assert.assertSame(LINEAL, newGeometry);

  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final LinealEditor editor = LINEAL.newGeometryEditor(4);
      final double newValue = LINEAL.getCoordinate(1, 1, i);
      editor.setCoordinate(1, 1, i, newValue);
      final Lineal newGeometry = editor.newGeometry();
      Assert.assertNotSame(LINEAL, newGeometry);
      Assert.assertEquals(newValue, newGeometry.getCoordinate(1, 1, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final LinealEditor editor = LINEAL.newGeometryEditor(4);
    editor.setM(1, 1, 10);
    final Lineal newGeometry = editor.newGeometry();
    Assert.assertNotSame(LINEAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getM(1, 1), 0.0);
  }

  @Test
  public void testSetX() {
    final LinealEditor editor = LINEAL.newGeometryEditor(3);
    editor.setX(1, 1, 10);
    final Lineal newGeometry = editor.newGeometry();
    Assert.assertNotSame(LINEAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getX(1, 1), 0.0);
  }

  @Test
  public void testSetY() {
    final LinealEditor editor = LINEAL.newGeometryEditor(3);
    editor.setY(1, 1, 10);
    final Lineal newGeometry = editor.newGeometry();
    Assert.assertNotSame(LINEAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getY(1, 1), 0.0);
  }

  @Test
  public void testSetZ() {
    final LinealEditor editor = LINEAL.newGeometryEditor(3);
    editor.setZ(1, 1, 10);
    final Lineal newGeometry = editor.newGeometry();
    Assert.assertNotSame(LINEAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getZ(1, 1), 0.0);
  }
}
