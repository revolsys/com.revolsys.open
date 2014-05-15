package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

@SuppressWarnings("serial")
public class PointWithOrientation extends PointDouble {

  private final double orientation;

  public PointWithOrientation(final Point point, final double orientation) {
    super(point);
    this.orientation = orientation;
  }

  public double getOrientation() {
    return orientation;
  }
}
