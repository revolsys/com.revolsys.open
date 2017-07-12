package com.revolsys.gis.grid;

import java.nio.file.Path;
import java.util.List;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public interface RectangularMapGrid extends GeometryFactoryProxy, MapSerializer {

  static String getTileFileName(final String filePrefix, final int coordinateSystemId,
    final String gridTileOrCellSize, final int tileMinX, final int tileMinY,
    final String fileExtension) {
    return filePrefix + "_" + coordinateSystemId + "_" + gridTileOrCellSize + "_" + tileMinX + "_"
      + tileMinY + "." + fileExtension;
  }

  static Path getTilePath(final Path basePath, final String filePrefix,
    final int coordinateSystemId, final String gridTileOrCellSize, final int tileMinX,
    final int tileMinY, final String fileExtension) {
    final Path fileExtensionDirectory = basePath.resolve(fileExtension);
    final Path coordinateSystemDirectory = fileExtensionDirectory
      .resolve(Integer.toString(coordinateSystemId));
    final Path resolutionDirectory = coordinateSystemDirectory.resolve(gridTileOrCellSize);
    final Path directoryX = resolutionDirectory.resolve(Integer.toString(tileMinX));
    final String fileName = getTileFileName(filePrefix, coordinateSystemId, gridTileOrCellSize,
      tileMinX, tileMinY, fileExtension);
    return directoryX.resolve(fileName);
  }

  static Resource getTileResource(final Resource basePath, final String filePrefix,
    final int coordinateSystemId, final String gridTileOrCellSize, final int tileMinX,
    final int tileMinY, final String fileExtension) {
    final Resource fileExtensionDirectory = basePath.createRelative(fileExtension);
    final Resource coordinateSystemDirectory = fileExtensionDirectory
      .createRelative(Integer.toString(coordinateSystemId));
    final Resource resolutionDirectory = coordinateSystemDirectory
      .createRelative(gridTileOrCellSize);
    final Resource directoryX = resolutionDirectory.createRelative(Integer.toString(tileMinX));
    final String fileName = getTileFileName(filePrefix, coordinateSystemId, gridTileOrCellSize,
      tileMinX, tileMinY, fileExtension);
    return directoryX.createRelative(fileName);
  }

  default BoundingBox getBoundingBox(final String mapTileName, final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.convert(geometryFactory);
  }

  String getFormattedMapTileName(String name);

  String getMapTileName(final double x, final double y);

  default String getMapTileName(final Geometry geometry) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final Geometry projectedGeometry = geometry
      .convertGeometry(coordinateSystem.getGeometryFactory());
    final Point centroid = projectedGeometry.getCentroid();
    final Point coordinate = centroid.getPoint();
    final String mapsheet = getMapTileName(coordinate.getX(), coordinate.getY());
    return mapsheet;
  }

  String getName();

  default Polygon getPolygon(final String mapTileName, final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, coordinateSystem.getGeometryFactory());
  }

  default Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  default Polygon getPolygon(final String mapTileName, final GeometryFactory geometryFactory,
    final int numX, final int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  RecordDefinition getRecordDefinition();

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
