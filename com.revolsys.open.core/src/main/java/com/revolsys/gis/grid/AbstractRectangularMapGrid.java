package com.revolsys.gis.grid;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public abstract class AbstractRectangularMapGrid implements RectangularMapGrid {
  private String name;

  @Override
  public BoundingBox getBoundingBox(final String mapTileName, final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.convert(geometryFactory);
  }

  public String getMapTileName(final Geometry geometry) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final Geometry projectedGeometry = geometry.convert(GeometryFactory.getFactory(coordinateSystem));
    final Point centroid = projectedGeometry.getCentroid();
    final Coordinates coordinate = centroid.getCoordinate();
    final String mapsheet = getMapTileName(coordinate.getX(), coordinate.getY());
    return mapsheet;
  }

  @Override
  public String getName() {
    if (name == null) {
      return getClass().getName();
    } else {
      return name;
    }
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final int numX, final int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, GeometryFactory.getFactory(coordinateSystem));
  }

  public void setName(final String name) {
    this.name = name;
  }
}
