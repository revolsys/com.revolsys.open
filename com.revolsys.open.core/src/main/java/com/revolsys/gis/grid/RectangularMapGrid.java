package com.revolsys.gis.grid;

import java.util.List;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.spring.resource.Resource;

public interface RectangularMapGrid extends GeometryFactoryProxy, MapSerializer {
  static String getTileFileName(final int coordinateSystemId, final int gridCellSize,
    final int tileMinX, final int tileMinY, final String fileExtension) {
    return "dem_" + coordinateSystemId + "_" + gridCellSize + "m" + "_" + tileMinX + "_" + tileMinY
      + "." + fileExtension;
  }

  static Resource getTileResource(final Resource basePath, final int coordinateSystemId,
    final int gridCellSize, final int tileMinX, final int tileMinY, final String fileExtension) {
    final Resource fileExtensionDirectory = basePath.createRelative(fileExtension);
    final Resource coordinateSystemDirectory = fileExtensionDirectory
      .createRelative(Integer.toString(coordinateSystemId));
    final Resource resolutionDirectory = coordinateSystemDirectory
      .createRelative(gridCellSize + "m");
    final Resource rowDirectory = resolutionDirectory.createRelative(Integer.toString(tileMinX));
    final String fileName = getTileFileName(coordinateSystemId, gridCellSize, tileMinX, tileMinY,
      fileExtension);
    return rowDirectory.createRelative(fileName);
  }

  static Resource getTileResource(final Resource baseResource, final int coordinateSystemId,
    final int gridCellSize, final int gridSize, final String fileExtension, final double x,
    final double y) {
    final int gridHeight = gridSize;
    final int tileMinX = CustomRectangularMapGrid.getGridFloor(0.0, gridSize, x);
    final int tileMinY = CustomRectangularMapGrid.getGridFloor(0.0, gridHeight, y);
    return getTileResource(baseResource, coordinateSystemId, gridCellSize, tileMinX, tileMinY,
      fileExtension);
  }

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
  default MapEx toMap() {
    return new LinkedHashMapEx();
  }
}
