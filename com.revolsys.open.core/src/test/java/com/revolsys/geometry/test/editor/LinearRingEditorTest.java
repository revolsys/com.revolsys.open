package com.revolsys.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.editor.LineStringEditor;

public class LinearRingEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final LineString LINE_STRING = WGS84_2D.lineString(2, 100.0, 200.0, 110.0, 210.0);

  @Test
  public void testNotModified() {
    final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor();
    try {
      lineEditor.setZ(new int[0], 13);
      Assert.fail("Invalid index should cause exception");
    } catch (final Exception e) {
    }

  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor(4);
      final int newValue = i * 10;
      lineEditor.setCoordinate(0, i, newValue);
      final LineString newLineString = lineEditor.newGeometry();
      Assert.assertNotSame(LINE_STRING, newLineString);
      Assert.assertEquals(newValue, newLineString.getCoordinate(0, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor(4);
    lineEditor.setM(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(LINE_STRING, newLineString);
    Assert.assertEquals(10.0, newLineString.getM(0), 0.0);
  }

  @Test
  public void testSetX() {
    final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor(3);
    lineEditor.setX(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(LINE_STRING, newLineString);
    Assert.assertEquals(10.0, newLineString.getX(0), 0.0);
  }

  @Test
  public void testSetY() {
    final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor(3);
    lineEditor.setY(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(LINE_STRING, newLineString);
    Assert.assertEquals(10.0, newLineString.getY(0), 0.0);
  }

  @Test
  public void testSetZ() {
    final LineStringEditor lineEditor = LINE_STRING.newGeometryEditor(3);
    lineEditor.setZ(0, 10);
    final LineString newLineString = lineEditor.newGeometry();
    Assert.assertNotSame(LINE_STRING, newLineString);
    Assert.assertEquals(10.0, newLineString.getZ(0), 0.0);
  }
}
