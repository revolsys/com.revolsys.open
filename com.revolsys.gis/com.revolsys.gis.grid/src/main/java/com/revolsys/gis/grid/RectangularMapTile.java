package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.vividsolutions.jts.geom.Polygon;

public interface RectangularMapTile {
  BoundingBox getBoundingBox();

  RectangularMapGrid getGrid();

  String getName();

  String getFormattedName();

  Polygon getPolygon(
    int numPoints);
}
