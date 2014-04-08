package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.Polygon;

public interface RectangularMapTile {
  BoundingBox getBoundingBox();

  String getFormattedName();

  RectangularMapGrid getGrid();

  String getName();

  Polygon getPolygon(com.revolsys.jts.geom.GeometryFactory factory, int numPoints);

  Polygon getPolygon(com.revolsys.jts.geom.GeometryFactory factory, final int numXPoints,
    final int numYPoints);

  Polygon getPolygon(int numPoints);

  Polygon getPolygon(final int numXPoints, final int numYPoints);
}
