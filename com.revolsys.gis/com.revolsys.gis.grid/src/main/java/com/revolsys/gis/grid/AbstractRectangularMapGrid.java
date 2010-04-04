package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public abstract class AbstractRectangularMapGrid implements RectangularMapGrid {
  public Polygon getPolygon(
    final String mapTileName,
    final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, new GeometryFactory(coordinateSystem));
  }

  public Polygon getPolygon(
    String mapTileName,
    GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }
}
