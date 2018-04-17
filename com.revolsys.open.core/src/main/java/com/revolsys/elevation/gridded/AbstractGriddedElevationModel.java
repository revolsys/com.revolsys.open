package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.AbstractGrid;

public abstract class AbstractGriddedElevationModel extends AbstractGrid
  implements GriddedElevationModel {

  public AbstractGriddedElevationModel() {
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellSize) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellheight) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth, gridCellheight);
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final int gridWidth,
    final double gridCellSize) {
    super(geometryFactory, gridWidth, gridCellSize);
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final int gridWidth,
    final double gridCellWidth, final double gridCellHeight) {
    super(geometryFactory, gridWidth, gridCellWidth, gridCellHeight);
  }

}
