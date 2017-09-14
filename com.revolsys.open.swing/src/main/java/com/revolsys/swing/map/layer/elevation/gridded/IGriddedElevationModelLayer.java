package com.revolsys.swing.map.layer.elevation.gridded;

import com.revolsys.geometry.model.Point;
import com.revolsys.swing.map.layer.Layer;

public interface IGriddedElevationModelLayer extends Layer {
  double getElevation(final double x, double y);

  default double getElevation(final Point point) {
    final Point convertedPoint = convertGeometry(point);
    final double x = convertedPoint.getX();
    final double y = convertedPoint.getY();
    return getElevation(x, y);
  }

  void redraw();
}
