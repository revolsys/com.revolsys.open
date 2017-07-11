package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;

public class LinearRingEditor extends LineStringEditor implements LinearRing {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LinearRingEditor(final LinearRing ring) {
    super(ring);
  }

  @Override
  public LinearRing clone() {
    return (LinearRing)super.clone();
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
  public String toString() {
    return toEwkt();
  }
}
