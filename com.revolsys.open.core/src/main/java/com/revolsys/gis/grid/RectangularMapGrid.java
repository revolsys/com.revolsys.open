package com.revolsys.gis.grid;

import java.util.List;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;

public interface RectangularMapGrid {
  BoundingBox getBoundingBox(final String mapTileName, final int srid);

  CoordinateSystem getCoordinateSystem();

  String getFormattedMapTileName(String name);

  GeometryFactory getGeometryFactory();

  String getMapTileName(final double x, final double y);

  String getName();

  Polygon getPolygon(final String mapTileName,
    final CoordinateSystem coordinateSystem);

  Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory);

  Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory, int numX,
    int numY);

  RectangularMapTile getTileByLocation(double x, double y);

  RectangularMapTile getTileByName(String name);

  double getTileHeight();

  List<RectangularMapTile> getTiles(final BoundingBox boundingBox);

  double getTileWidth();
}
