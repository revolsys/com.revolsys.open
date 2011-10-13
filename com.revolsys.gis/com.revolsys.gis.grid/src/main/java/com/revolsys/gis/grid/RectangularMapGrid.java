package com.revolsys.gis.grid;

import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public interface RectangularMapGrid {
  CoordinateSystem getCoordinateSystem();

  String getFormattedMapTileName(String name);

  String getMapTileName(final double x, final double y);

  RectangularMapTile getTileByLocation(double x, double y);

  RectangularMapTile getTileByName(String name);

  double getTileHeight();

  List<RectangularMapTile> getTiles(final BoundingBox boundingBox);

  double getTileWidth();

  BoundingBox getBoundingBox(final String mapTileName,
    final int srid);

  Polygon getPolygon(final String mapTileName,
    final CoordinateSystem coordinateSystem);

  Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory);

  Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory, int numX, int numY);
}
