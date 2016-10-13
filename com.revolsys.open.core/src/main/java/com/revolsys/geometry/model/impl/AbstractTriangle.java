package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Triangle;

public abstract class AbstractTriangle extends AbstractPolygon implements Triangle {
  @Override
  public Triangle clone() {
    return (Triangle)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Triangle) {
      final Triangle triangle = (Triangle)other;
      return equals(triangle);
    } else {
      return super.equals(other);
    }
  }
}
