package com.revolsys.core.test.geometry.test.editor;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.editor.PunctualEditor;

public class MultiPointEditorTest {
  private static final GeometryFactory WGS84_2D = GeometryFactory.wgs84().convertAxisCount(2);

  private static final Punctual PUNCTUAL = WGS84_2D.punctual(2, 100.0, 200.0, 110.0, 210.0);

  @Test
  public void testNotModified() {
    final PunctualEditor editor = PUNCTUAL.newGeometryEditor();
    editor.setZ(new int[0], 13);
    final Punctual newMultiPoint = editor.newGeometry();
    Assert.assertSame(PUNCTUAL, newMultiPoint);

  }

  @Test
  public void testSetCoordinates() {
    for (int i = 0; i < 4; i++) {
      final PunctualEditor editor = PUNCTUAL.newGeometryEditor(4);
      final int newValue = i * 10;
      editor.setCoordinate(0, i, newValue);
      final Punctual newMultiPoint = editor.newGeometry();
      Assert.assertNotSame(PUNCTUAL, newMultiPoint);
      Assert.assertEquals(newValue, newMultiPoint.getCoordinate(0, i), 0.0);
    }
  }

  @Test
  public void testSetM() {
    final PunctualEditor editor = PUNCTUAL.newGeometryEditor(4);
    editor.setM(0, 10);
    final Punctual newMultiPoint = editor.newGeometry();
    Assert.assertNotSame(PUNCTUAL, newMultiPoint);
    Assert.assertEquals(10.0, newMultiPoint.getM(0), 0.0);
  }

  @Test
  public void testSetX() {
    final PunctualEditor editor = PUNCTUAL.newGeometryEditor(3);
    editor.setX(0, 10);
    final Punctual newMultiPoint = editor.newGeometry();
    Assert.assertNotSame(PUNCTUAL, newMultiPoint);
    Assert.assertEquals(10.0, newMultiPoint.getX(0), 0.0);
  }

  @Test
  public void testSetY() {
    final PunctualEditor editor = PUNCTUAL.newGeometryEditor(3);
    editor.setY(0, 10);
    final Punctual newMultiPoint = editor.newGeometry();
    Assert.assertNotSame(PUNCTUAL, newMultiPoint);
    Assert.assertEquals(10.0, newMultiPoint.getY(0), 0.0);
  }

  @Test
  public void testSetZ() {
    final PunctualEditor editor = PUNCTUAL.newGeometryEditor(3);
    editor.setZ(0, 10);
    final Punctual newMultiPoint = editor.newGeometry();
    Assert.assertNotSame(PUNCTUAL, newMultiPoint);
    Assert.assertEquals(10.0, newMultiPoint.getZ(0), 0.0);
  }
}
