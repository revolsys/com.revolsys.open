package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Point;

public class PointDoubleXYBuilder extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  public PointDoubleXYBuilder() {
  }

  public PointDoubleXYBuilder(final double x, final double y) {
    super(x, y);
  }

  public PointDoubleXYBuilder(final Point point) {
    super(point);
  }

  @Override
  public double setX(final double x) {
    final double oldValue = getX();
    super.setXDo(x);
    return oldValue;
  }

  @Override
  public double setY(final double y) {
    final double oldValue = getY();
    super.setYDo(y);
    return oldValue;
  }
}
