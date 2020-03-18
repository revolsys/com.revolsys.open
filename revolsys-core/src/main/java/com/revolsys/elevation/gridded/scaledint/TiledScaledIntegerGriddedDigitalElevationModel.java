package com.revolsys.elevation.gridded.scaledint;

import java.nio.file.Path;
import java.util.Map;

import com.revolsys.collection.map.LruMap;
import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.AbstractGrid;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.IntPair;
import com.revolsys.util.Strings;

public class TiledScaledIntegerGriddedDigitalElevationModel extends AbstractGrid
  implements GriddedElevationModel {

  private int gridTileSize;

  private final int coordinateSystemId;

  private final Map<IntPair, DirectFileElevationModel> models = new LruMap<>(5000);

  private final Path baseDirectory;

  private final IntPair getKey = new IntPair();

  private final String filePrefix;

  private final int gridCellSize;

  private final String tileWidthString;

  public TiledScaledIntegerGriddedDigitalElevationModel(final Path baseDirectory,
    final String filePrefix, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileSize, final int gridCellSize) {
    super(geometryFactory, minX, minY, Integer.MAX_VALUE, Integer.MAX_VALUE, gridCellSize);
    this.filePrefix = filePrefix;
    this.gridTileSize = gridTileSize;
    this.gridCellSize = gridCellSize;
    this.coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    this.tileWidthString = Integer.toString(gridCellSize * gridTileSize);
    this.baseDirectory = baseDirectory//
      .resolve(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION)//
      .resolve(Integer.toString(this.coordinateSystemId)) //
      .resolve(this.tileWidthString)//
    ;
  }

  public TiledScaledIntegerGriddedDigitalElevationModel(final Resource baseResource,
    final String filePrefix, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileSize, final int gridCellSize) {
    this(baseResource.toPath(), filePrefix, geometryFactory, minX, minY, gridTileSize,
      gridCellSize);
  }

  @Override
  public void clear() {
  }

  public int getGridTileSize() {
    return this.gridTileSize;
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    DirectFileElevationModel model;
    final Map<IntPair, DirectFileElevationModel> models = this.models;
    final int tileSize = this.gridTileSize;
    final int minGridX = Math.floorDiv(gridX, tileSize);
    final int minGridY = Math.floorDiv(gridY, tileSize);
    synchronized (models) {
      final IntPair getKey = this.getKey;
      getKey.setValues(minGridX, minGridY);

      model = models.get(getKey);
      if (model == null) {
        final int cellSize = this.gridCellSize;
        final int tileX = minGridX * tileSize * cellSize;
        final int tileY = minGridY * tileSize * cellSize;
        final GeometryFactory geometryFactory = getGeometryFactory();

        final String fileName = Strings.toString("_", this.filePrefix,
          getHorizontalCoordinateSystemId(), this.tileWidthString, tileX, tileY) + "."
          + ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION;
        final Path path = this.baseDirectory //
          .resolve(Integer.toString(tileX)) //
          .resolve(fileName);

        model = new ScaledIntegerGriddedDigitalElevationModelFile(path, geometryFactory, tileX,
          tileY, tileSize, tileSize, this.gridCellWidth);
        models.put(getKey.clone(), model);
      }
    }
    final int gridCellX = gridX % tileSize;
    final int gridCellY = gridY % tileSize;

    return model.getValue(gridCellX, gridCellY);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  @Override
  public GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellSize) {
    throw new UnsupportedOperationException();
  }

  public void setGridTileSize(final int gridTileSize) {
    this.gridTileSize = gridTileSize;
  }

  @Override
  public void setValue(final int x, final int y, final double elevation) {
  }

}
