package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.util.MathUtil;

public class CustomRectangularMapGrid extends AbstractRectangularMapGrid {
  private static final double DEFAULT_TILE_SIZE = 1000;

  public static int getGridCeil(final double origin, final double gridSize, final double value) {
    final int xIndex = (int)Math.ceil((value - origin) / gridSize);
    final double gridValue = origin + xIndex * gridSize;
    return (int)gridValue;
  }

  public static int getGridFloor(final double origin, final double gridSize, final double value) {
    final int xIndex = (int)Math.floor((value - origin) / gridSize);
    final double gridValue = origin + xIndex * gridSize;
    return (int)gridValue;
  }

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT;

  private double originX = 0.0;

  private double originY = 0.0;

  private double tileHeight = DEFAULT_TILE_SIZE;

  private double tileWidth = DEFAULT_TILE_SIZE;

  public CustomRectangularMapGrid() {
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory) {
    this(geometryFactory, 0, 0, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double tileSize) {
    this(geometryFactory, 0, 0, tileSize, tileSize);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double tileWidth,
    final double tileHeight) {
    this(geometryFactory, 0, 0, tileWidth, tileHeight);
  }

  public CustomRectangularMapGrid(final GeometryFactory geometryFactory, final double originX,
    final double originY, final double tileWidth, final double tileHeight) {
    this.geometryFactory = geometryFactory;
    this.tileHeight = tileHeight;
    this.tileWidth = tileWidth;
    this.originX = originX;
    this.originY = originY;
  }

  public CustomRectangularMapGrid(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public List<RectangularMapTile> getAllTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.convert(getGeometryFactory());

    final List<RectangularMapTile> tiles = new ArrayList<>();
    final int minX = getGridFloor(this.originX, this.tileWidth, envelope.getMinX());
    final int minY = getGridFloor(this.originY, this.tileHeight, envelope.getMinY());
    final int maxX = getGridCeil(this.originX, this.tileWidth, envelope.getMaxX());
    final int maxY = getGridCeil(this.originY, this.tileHeight, envelope.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(x, y);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  public BoundingBox getBoundingBox(final String name) {
    final double[] coordinates = MathUtil.toDoubleArraySplit(name, "_");
    if (coordinates.length == 2) {
      final double x1 = coordinates[0];
      final double y1 = coordinates[1];
      final double x2 = x1 + this.tileWidth;
      final double y2 = y1 + this.tileHeight;
      return this.geometryFactory.newBoundingBox(x1, y1, x2, y2);
    } else {
      return null;
    }
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return this.geometryFactory.getCoordinateSystem();
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final int tileX = getGridFloor(this.originX, this.tileWidth, x);
    final int tileY = getGridFloor(this.originY, this.tileHeight, y);

    return tileX + "_" + tileY;
  }

  public String getMapTileName(final Point coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return getMapTileName(x, y);
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String name = getMapTileName(x, y);
    if (name == null) {
      return null;
    } else {
      return getTileByName(name);
    }
  }

  @Override
  public RectangularMapTile getTileByName(final String name) {
    final BoundingBox boundingBox = getBoundingBox(name);
    if (boundingBox == null) {
      return null;
    } else {
      return new SimpleRectangularMapTile(this, name, name, boundingBox);
    }
  }

  @Override
  public double getTileHeight() {
    return this.tileHeight;
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.convert(getGeometryFactory());

    final List<RectangularMapTile> tiles = new ArrayList<>();
    final int minX = getGridFloor(this.originX, this.tileWidth, envelope.getMinX());
    final int minY = getGridFloor(this.originY, this.tileHeight, envelope.getMinY());
    final int maxX = getGridCeil(this.originX, this.tileWidth, envelope.getMaxX());
    final int maxY = getGridCeil(this.originY, this.tileHeight, envelope.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    if (numX > 20 || numY > 20) {
      return tiles;
    }
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(x, y);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @Override
  public double getTileWidth() {
    return this.tileWidth;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setOriginX(final double originX) {
    this.originX = originX;
  }

  public void setOriginY(final double originY) {
    this.originY = originY;
  }

  public void setSrid(final int srid) {
    setGeometryFactory(GeometryFactory.fixed(srid, 1.0));
  }

  public void setTileHeight(final double tileHeight) {
    this.tileHeight = tileHeight;
  }

  public void setTileSize(final double tileSize) {
    setTileWidth(tileSize);
    setTileHeight(tileSize);
  }

  public void setTileWidth(final double tileWidth) {
    this.tileWidth = tileWidth;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.put(MapObjectFactory.TYPE, "customRectangularMapGrid");
    addToMap(map, "geometryFactory", getGeometryFactory());
    addToMap(map, "originX", getOriginX());
    addToMap(map, "originY", getOriginY());
    addToMap(map, "tileWidth", getTileWidth());
    addToMap(map, "tileHeight", getTileHeight());
    return map;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    if (this.geometryFactory != null) {
      string.append(this.geometryFactory.getCoordinateSystem().getCoordinateSystemName());
      string.append(" ");
    }
    if (this.originX != 0 && this.originY != 0) {
      string.append(this.originX);
      string.append(',');
      string.append(this.originY);
    }

    string.append(this.tileWidth);
    string.append('x');
    string.append(this.tileHeight);

    return string.toString();
  }
}
