package com.revolsys.gis.grid;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.map.MapSerializer;

public interface RectangularMapGrid extends GeometryFactoryProxy, MapSerializer {
  BoundingBox getBoundingBox(final String mapTileName, final int srid);

  String getFormattedMapTileName(String name);

  String getMapTileName(final double x, final double y);

  String getName();

  Polygon getPolygon(final String mapTileName, final CoordinateSystem coordinateSystem);

  Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory);

  Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory, int numX,
    int numY);

  RectangularMapTile getTileByLocation(double x, double y);

  RectangularMapTile getTileByName(String name);

  double getTileHeight();

  List<RectangularMapTile> getTiles(final BoundingBox boundingBox);

  double getTileWidth();

  @Override
  default Map<String, Object> toMap() {
    return new LinkedHashMap<>();
  }
}
