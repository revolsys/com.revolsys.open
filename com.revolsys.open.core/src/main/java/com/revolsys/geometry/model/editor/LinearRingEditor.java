package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;

public class LinearRingEditor extends LineStringEditor implements LinearRing {
  private static final long serialVersionUID = 1L;

  public LinearRingEditor(final AbstractGeometryEditor<?> parentEditor) {
    this(parentEditor, parentEditor.getGeometryFactory().linearRing());
  }

  public LinearRingEditor(final AbstractGeometryEditor<?> parentEditor, final LinearRing ring) {
    super(parentEditor, ring);
  }

  public LinearRingEditor(final LinearRing ring) {
    super(ring);
  }

  @Override
  public LinearRingEditor clone() {
    return (LinearRingEditor)super.clone();
  }

  @Override
  public LinearRing getOriginalGeometry() {
    return (LinearRing)super.getOriginalGeometry();
  }

  @Override
  public LinearRing newGeometry() {
    return (LinearRing)super.newGeometry();
  }

  @Override
  public LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return LinearRing.super.newGeometry(geometryFactory);
  }

  @Override
  public double setCoordinate(final int vertexIndex, final int axisIndex, final double coordinate) {
    final int lastVertexIndex = getLastVertexIndex();
    if (vertexIndex == 0 || vertexIndex == lastVertexIndex) {
      // Ensure valid loop
      super.setCoordinate(0, axisIndex, coordinate);
      return super.setCoordinate(lastVertexIndex, axisIndex, coordinate);
    } else {
      return super.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
