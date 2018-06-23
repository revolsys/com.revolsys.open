package com.revolsys.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.editor.LinearRingEditor;

public class LinearRingEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final GeometryFactory WGS84_3D = GeometryFactory.wgs84().convertAxisCount(3);

  private static final LinearRing RING_2D = WGS84_2D.linearRing(2, //
    0.0, 0.0, //
    0.0, 10.0, //
    10.0, 10.0, //
    10.0, 0.0, //
    0.0, 0.0//
  );

  private static final LinearRing RING_3D = WGS84_3D.linearRing(3, //
    0.0, 0.0, 1, //
    0.0, 10.0, 2, //
    10.0, 10.0, 3, //
    10.0, 0.0, 4, //
    0.0, 0.0, 1//
  );

  @Test
  public void testDeleteVertex() {
    final LinearRingEditor editor = RING_3D.newGeometryEditor(3);
    {
      editor.deleteVertex(0);
      final LinearRing expected = WGS84_3D.linearRing(3, //
        0.0, 10.0, 2, //
        10.0, 10.0, 3, //
        10.0, 0.0, 4, //
        0.0, 10.0, 2//
      );
      Assert.assertEquals("deleteVertex(0)", expected, editor);
    }
    {
      editor.revertChanges();
      editor.deleteVertex(4);
      final LinearRing expected = WGS84_3D.linearRing(3, //
        10.0, 0.0, 4, //
        0.0, 10.0, 2, //
        10.0, 10.0, 3, //
        10.0, 0.0, 4 //
      );
      Assert.assertEquals("deleteVertex(0)", expected, editor);
    }
  }

  @Test
  public void testNotModified() {
    final LinearRingEditor lineEditor = RING_2D.newGeometryEditor();
    try {
      lineEditor.setZ(new int[0], 13);
      Assert.fail("Invalid index should cause exception");
    } catch (final Exception e) {
    }

  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final LinearRingEditor lineEditor = RING_2D.newGeometryEditor(4);
      final int newValue = i * 10;
      lineEditor.setCoordinate(0, i, newValue);
      final LineString newLineString = lineEditor.newGeometry();
      Assert.assertNotSame(RING_2D, newLineString);
      Assert.assertEquals(newValue, newLineString.getCoordinate(0, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final LinearRingEditor lineEditor = RING_2D.newGeometryEditor(4);
    lineEditor.setM(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(RING_2D, newLineString);
    Assert.assertEquals(10.0, newLineString.getM(0), 0.0);
  }

  @Test
  public void testSetX() {
    final LinearRingEditor lineEditor = RING_2D.newGeometryEditor(3);
    lineEditor.setX(1, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(RING_2D, newLineString);
    Assert.assertEquals(10.0, newLineString.getX(1), 0.0);
  }

  @Test
  public void testSetY() {
    final LinearRingEditor lineEditor = RING_2D.newGeometryEditor(3);
    lineEditor.setY(1, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(RING_2D, newLineString);
    Assert.assertEquals(10.0, newLineString.getY(1), 0.0);
  }

  @Test
  public void testSetZ() {
    final LinearRingEditor lineEditor = RING_2D.newGeometryEditor(3);
    lineEditor.setZ(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(RING_2D, newLineString);
    Assert.assertEquals(10.0, newLineString.getZ(0), 0.0);
  }
}
