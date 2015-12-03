package com.revolsys.geometry.model.vertex;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.math.Angle;

public abstract class AbstractVertex extends AbstractPoint implements Vertex {
  private static final long serialVersionUID = 1L;

  protected final Geometry geometry;

  public AbstractVertex(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public AbstractVertex clone() {
    return (AbstractVertex)super.clone();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry() {
    return (V)this.geometry;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometry.getGeometryFactory();
  }

  @Override
  public double getOrientaton() {
    if (isEmpty()) {
      return 0;
    } else {
      final double x = getX();
      final double y = getY();
      double angle;
      if (isFrom()) {
        final double x1 = getLineCoordinateRelative(1, 0);
        final double y1 = getLineCoordinateRelative(1, 1);
        angle = Angle.angleDegrees(x, y, x1, y1);
      } else {
        final double x1 = getLineCoordinateRelative(-1, 0);
        final double y1 = getLineCoordinateRelative(-1, 1);
        angle = Angle.angleDegrees(x1, y1, x, y);
      }
      if (Double.isNaN(angle)) {
        return 0;
      } else {
        return angle;
      }
    }
  }
}
