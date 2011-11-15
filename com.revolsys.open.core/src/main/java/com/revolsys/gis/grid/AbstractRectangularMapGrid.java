package com.revolsys.gis.grid;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class AbstractRectangularMapGrid implements RectangularMapGrid {
  public Polygon getPolygon(final String mapTileName,
    final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, new GeometryFactory(coordinateSystem));
  }

  public BoundingBox getBoundingBox(String mapTileName, int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.convert(geometryFactory);
  }

  public Polygon getPolygon(String mapTileName, GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  public Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory, int numX, int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  public String getMapTileName(final Geometry geometry) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final Geometry projectedGeometry = GeometryProjectionUtil.perform(geometry,
      coordinateSystem);
    final Point centroid = projectedGeometry.getCentroid();
    final Coordinate coordinate = centroid.getCoordinate();
    final String mapsheet = getMapTileName(coordinate.x, coordinate.y);
    return mapsheet;
  }
}
