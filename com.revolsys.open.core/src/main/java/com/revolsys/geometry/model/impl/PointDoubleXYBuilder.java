package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Point;

public class PointDoubleXYBuilder extends PointDoubleXY {

  public PointDoubleXYBuilder() {
  }

  public PointDoubleXYBuilder(final double x, final double y) {
    super(x, y);
  }

  public PointDoubleXYBuilder(final Point point) {
    super(point);
  }

  @Override
  public void setX(final double x) {
    super.setX(x);
  }

  @Override
  public void setY(final double y) {
    super.setY(y);
  }
}
