package com.revolsys.gis.grid;

import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public interface RectangularMapGrid {
  CoordinateSystem getCoordinateSystem();

  String getFormattedMapTileName(
    String name);

  public String getMapTileName(
    final double x,
    final double y);

  RectangularMapTile getTileByLocation(
    double x,
    double y);

  RectangularMapTile getTileByName(
    String name);

  double getTileHeight();

  List<RectangularMapTile> getTiles(
    final BoundingBox boundingBox);

  double getTileWidth();

  public Polygon getPolygon(
    final String mapTileName,
    final CoordinateSystem coordinateSystem);

  public Polygon getPolygon(
    final String mapTileName,
    final GeometryFactory geometryFactory);
}
