package com.revolsys.elevation.gridded;

import java.awt.image.DataBuffer;

public class GriddedElevationModelDataBuffer extends DataBuffer {
  private final GriddedElevationModel elevationModel;

  private final int width;

  private final int height;

  public GriddedElevationModelDataBuffer(final GriddedElevationModel elevationModel) {
    super(TYPE_INT, elevationModel.getGridWidth() * elevationModel.getGridHeight());
    this.elevationModel = elevationModel;
    this.width = elevationModel.getGridWidth();
    this.height = elevationModel.getGridHeight();
  }

  @Override
  public int getElem(final int bank, final int i) {
    if (bank == 0) {
      final int x = i % this.width;
      final int y = this.height - 1 - (i - x) / this.width;
      return this.elevationModel.getColour(x, y);
    } else {
      return 0;
    }
  }

  @Override
  public void setElem(final int bank, final int i, final int val) {
    throw new UnsupportedOperationException();
  }
}
