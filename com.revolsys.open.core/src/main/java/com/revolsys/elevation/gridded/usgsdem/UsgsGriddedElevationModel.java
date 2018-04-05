package com.revolsys.elevation.gridded.usgsdem;

import com.revolsys.elevation.gridded.AbstractGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class UsgsGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private final UsgsGriddedElevationModelColumn[] columns;

  public UsgsGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final int gridCellSize) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.columns = new UsgsGriddedElevationModelColumn[gridWidth];
  }

  @Override
  protected void expandZ() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (final UsgsGriddedElevationModelColumn column : this.columns) {
      for (final int elevationInt : column.elevations) {
        if (elevationInt != NULL_VALUE) {
          if (elevationInt < min) {
            min = elevationInt;
          }
          if (elevationInt > max) {
            max = elevationInt;
          }
        }
      }
    }
    final double minZ = toDoubleZ(min);
    final double maxZ = toDoubleZ(max);
    setZRange(minZ, maxZ);

  }

  @Override
  public double getElevationFast(final int gridX, final int gridY) {
    final UsgsGriddedElevationModelColumn column = this.columns[gridX];
    if (column != null) {
      final int elevationInt = column.getElevationInt(gridY);
      if (elevationInt != NULL_VALUE) {
        return toDoubleZ(elevationInt);
      }
    }
    return Double.NaN;
  }

  public int getElevationInt(final int gridX, final int gridY) {
    if (gridX >= 0 && gridX < this.gridWidth) {
      final UsgsGriddedElevationModelColumn column = this.columns[gridX];
      if (column != null) {
        return column.getElevationInt(gridY);
      }
    }
    return NULL_VALUE;
  }

  @Override
  public boolean hasElevationFast(final int gridX, final int gridY) {
    if (gridX >= 0 && gridX < this.gridWidth) {
      final UsgsGriddedElevationModelColumn column = this.columns[gridX];
      if (column != null) {
        return column.hasElevation(gridY);
      }
    }
    return false;
  }

  void setColumn(final int gridX, final int gridY, final int[] elevations) {
    this.columns[gridX] = new UsgsGriddedElevationModelColumn(gridY, elevations);
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory.getScaleZ() <= 0) {
      throw new IllegalArgumentException("Geometry factory must have a z scale factor");
    }
    super.setGeometryFactory(geometryFactory);
  }

  public void update(final double minX, final double minY, final double minZ, final Double maxZ,
    final int gridHeight) {
    this.gridHeight = gridHeight;
    final double maxX = minX + this.gridWidth * this.gridCellSize;
    final double maxY = minY + gridHeight * this.gridCellSize;
    setBounds(minX, minY, minZ, maxX, maxY, maxZ);
  }
}
