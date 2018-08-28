package com.revolsys.core.test.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.editor.PolygonalEditor;

public class MultiPolygonEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final Polygonal POLYGONAL = WGS84_2D.polygonal(//
    WGS84_2D.polygon(//
      WGS84_2D.linearRing(2, 0.0, 0.0, 0, 10, 10, 10, 10, 0, 0, 0), //
      WGS84_2D.linearRing(2, 2.0, 2.0, 2, 8, 8, 8, 8, 2, 2, 2) //
    ), //
    WGS84_2D.polygon(//
      WGS84_2D.linearRing(2, 0.0, 0.0, 0, 10, 10, 10, 10, 0, 0, 0), //
      WGS84_2D.linearRing(2, 2.0, 2.0, 2, 8, 8, 8, 8, 2, 2, 2)//
    )//
  );

  @Test
  public void testNotModified() {
    final PolygonalEditor editor = POLYGONAL.newGeometryEditor();
    editor.setZ(new int[0], 13);
    final Polygonal newGeometry = editor.newGeometry();
    Assert.assertSame(POLYGONAL, newGeometry);

  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final PolygonalEditor editor = POLYGONAL.newGeometryEditor(4);
      final double newValue = POLYGONAL.getCoordinate(1, 1, 1, i);
      editor.setCoordinate(1, 1, 1, i, newValue);
      final Polygonal newGeometry = editor.newGeometry();
      Assert.assertNotSame(POLYGONAL, newGeometry);
      Assert.assertEquals(newValue, newGeometry.getCoordinate(1, 1, 1, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final PolygonalEditor editor = POLYGONAL.newGeometryEditor(4);
    editor.setM(1, 1, 1, 10);
    final Polygonal newGeometry = editor.newGeometry();
    Assert.assertNotSame(POLYGONAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getM(1, 1, 1), 0.0);
  }

  @Test
  public void testSetX() {
    final PolygonalEditor editor = POLYGONAL.newGeometryEditor(3);
    editor.setX(1, 1, 1, 10);
    final Polygonal newGeometry = editor.newGeometry();
    Assert.assertNotSame(POLYGONAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getX(1, 1, 1), 0.0);
  }

  @Test
  public void testSetY() {
    final PolygonalEditor editor = POLYGONAL.newGeometryEditor(3);
    editor.setY(1, 1, 1, 10);
    final Polygonal newGeometry = editor.newGeometry();
    Assert.assertNotSame(POLYGONAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getY(1, 1, 1), 0.0);
  }

  @Test
  public void testSetZ() {
    final PolygonalEditor editor = POLYGONAL.newGeometryEditor(3);
    editor.setZ(1, 1, 1, 10);
    final Polygonal newGeometry = editor.newGeometry();
    Assert.assertNotSame(POLYGONAL, newGeometry);
    Assert.assertEquals(10.0, newGeometry.getZ(1, 1, 1), 0.0);
  }
}
