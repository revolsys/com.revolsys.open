package com.revolsys.gis.grid;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public abstract class AbstractRectangularMapGrid implements RectangularMapGrid {
  private String name;

  @Override
  public BoundingBox getBoundingBox(final String mapTileName, final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.convert(geometryFactory);
  }

  public String getMapTileName(final Geometry geometry) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final Geometry projectedGeometry = geometry.convertGeometry(coordinateSystem.getGeometryFactory());
    final Point centroid = projectedGeometry.getCentroid();
    final Point coordinate = centroid.getPoint();
    final String mapsheet = getMapTileName(coordinate.getX(), coordinate.getY());
    return mapsheet;
  }

  @Override
  public String getName() {
    if (this.name == null) {
      return getClass().getName();
    } else {
      return this.name;
    }
  }

  @Override
  public Polygon getPolygon(final String mapTileName, final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, coordinateSystem.getGeometryFactory());
  }

  @Override
  public Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  @Override
  public Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory,
    final int numX, final int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
