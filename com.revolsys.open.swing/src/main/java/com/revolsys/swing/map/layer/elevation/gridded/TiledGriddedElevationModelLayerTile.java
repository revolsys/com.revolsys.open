package com.revolsys.swing.map.layer.elevation.gridded;

import java.io.FileNotFoundException;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.layer.tile.AbstractMapTile;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Strings;

public class TiledGriddedElevationModelLayerTile extends AbstractMapTile<GriddedElevationModel> {
  private final TiledGriddedElevationModelLayer layer;

  private final int coordinateSystemId;

  private final int tileX;

  private final int tileY;

  private final int tileSize;

  public TiledGriddedElevationModelLayerTile(final TiledGriddedElevationModelLayer layer,
    final BoundingBox tileBoundingBox, final int coordinateSystemId, final int tileSize,
    final int resolution, final int tileX, final int tileY) {
    super(tileBoundingBox, tileSize / resolution, tileSize / resolution, resolution);
    this.layer = layer;
    this.coordinateSystemId = coordinateSystemId;
    this.tileSize = tileSize;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof TiledGriddedElevationModelLayerTile) {
      final TiledGriddedElevationModelLayerTile tile = (TiledGriddedElevationModelLayerTile)obj;
      if (tile.getCoordinateSystemId() == getCoordinateSystemId()) {
        if (tile.getTileSize() == getTileSize()) {
          if (tile.getTileX() == getTileX()) {
            if (tile.getTileY() == getTileY()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.coordinateSystemId;
  }

  public GriddedElevationModel getElevationModel() {
    return getData();
  }

  public int getTileSize() {
    return this.tileSize;
  }

  public int getTileX() {
    return this.tileX;
  }

  public int getTileY() {
    return this.tileY;
  }

  @Override
  public int hashCode() {
    return this.tileSize << 24 & this.tileX << 16 & this.tileY << 8;
  }

  @Override
  protected GriddedElevationModel loadDataDo() {
    try {
      return this.layer.newGriddedElevationModel(this.tileSize, this.tileX, this.tileY);
    } catch (final RuntimeException t) {
      if (!Exceptions.isException(t, FileNotFoundException.class)) {
        this.layer.setError(t);
      }
      return null;
    }
  }

  @Override
  public String toString() {
    return Strings.toString("_", new int[] {
      this.coordinateSystemId, this.tileSize, this.tileX, this.tileY
    });
  }
}
