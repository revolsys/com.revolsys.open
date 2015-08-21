package com.revolsys.gis.grid;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;

public interface RectangularMapTile {
  BoundingBox getBoundingBox();

  String getFormattedName();

  RectangularMapGrid getGrid();

  String getName();

  Polygon getPolygon(com.revolsys.geometry.model.GeometryFactory factory, int numPoints);

  Polygon getPolygon(com.revolsys.geometry.model.GeometryFactory factory, final int numXPoints,
    final int numYPoints);

  Polygon getPolygon(int numPoints);

  Polygon getPolygon(final int numXPoints, final int numYPoints);
}
