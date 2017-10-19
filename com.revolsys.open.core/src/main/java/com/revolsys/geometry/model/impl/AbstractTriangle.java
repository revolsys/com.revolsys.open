package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Triangle;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.function.BiFunctionDouble;
import com.revolsys.util.function.Function4Double;

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
  public <R> R findSegment(final Function4Double<R> action) {
    if (!isEmpty()) {
      final double x1 = getX(0);
      final double y1 = getY(0);
      final double x2 = getX(1);
      final double y2 = getY(1);
      final double x3 = getX(2);
      final double y3 = getY(2);
      R result = action.accept(x1, y1, x2, y2);
      if (result == null) {
        result = action.accept(x2, y2, x3, y3);
        if (result == null) {
          result = action.accept(x3, y3, x1, y1);
        }
      }
    }
    return null;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        final R result = action.accept(x, y);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
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
