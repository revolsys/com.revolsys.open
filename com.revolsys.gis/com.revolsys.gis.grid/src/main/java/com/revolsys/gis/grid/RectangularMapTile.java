package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public interface RectangularMapTile {
  BoundingBox getBoundingBox();

  String getFormattedName();

  RectangularMapGrid getGrid();

  String getName();

  Polygon getPolygon(
    GeometryFactory factory,
    int numPoints);

  Polygon getPolygon(
    int numPoints);
}
