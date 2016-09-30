package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.LinearRing;

public class LinearRingEditor extends LineStringEditor implements LinearRing {
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
  public String toString() {
    return toEwkt();
  }
}
