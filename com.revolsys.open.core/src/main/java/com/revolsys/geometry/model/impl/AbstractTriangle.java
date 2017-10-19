package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Triangle;
import com.revolsys.util.function.BiConsumerDouble;

public abstract class AbstractTriangle extends AbstractPolygon implements Triangle {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

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

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        action.accept(x, y);
      }
    }
  }
}
