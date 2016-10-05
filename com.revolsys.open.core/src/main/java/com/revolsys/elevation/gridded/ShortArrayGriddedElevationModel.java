package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.range.ShortMinMax;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.number.Doubles;

public class ShortArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  public static final short NULL_VALUE = Short.MIN_VALUE;

  private final short[] elevations;

  private ShortMinMax minMax;

  private float colorGreyMultiple;

  private short minZ;

  public ShortArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new short[gridWidth * gridHeight];
  }

  @Override
  public void clear() {
    Arrays.fill(this.elevations, NULL_VALUE);
    this.minMax = null;
  }

  @Override
  public int getColour(final int gridX, final int gridY) {
    final int offset = gridY * getGridHeight() + gridX;
    final int colour;
    final short elevation = this.elevations[offset];
    if (Doubles.equal(elevation, NULL_VALUE)) {
      colour = NULL_COLOUR;
    } else {
      final int grey = Math.round((elevation - this.minZ) * this.colorGreyMultiple);
      colour = WebColors.colorToRGB(255, grey, grey, grey);
    }
    return colour;
  }

  @Override
  public short getElevationShort(final int x, final int y) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
      final short elevation = this.elevations[y * gridHeight + x];
      return elevation;
    } else {
      return NULL_VALUE;
    }
  }

  public ShortMinMax getMinMax() {
    if (this.minMax == null) {
      this.minMax = ShortMinMax.newWithIgnore(NULL_VALUE, this.elevations);
      this.minZ = this.minMax.getMin();
      this.colorGreyMultiple = 255f / this.minMax.getRange();
    }
    return this.minMax;
  }

  @Override
  public boolean isEmpty() {
    final ShortMinMax minMax = getMinMax();
    return minMax.isEmpty();
  }

  @Override
  public boolean isNull(final int gridX, final int gridY) {
    final short elevation = getElevationShort(gridX, gridY);
    return elevation == NULL_VALUE;
  }

  @Override
  public BufferedImage newBufferedImage() {
    getMinMax();
    return super.newBufferedImage();
  }

  @Override
  public ShortArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new ShortArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final GriddedElevationModel elevationModel, final double x,
    final double y) {
    final short elevation = elevationModel.getElevationShort(x, y);
    setElevation(x, y, elevation);
  }

  @Override
  public void setElevation(final int gridX, final int gridY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
    if (!elevationModel.isNull(x, y)) {
      final short elevation = elevationModel.getElevationShort(x, y);
      setElevation(gridX, gridY, elevation);
    }
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final short elevation) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
      this.elevations[gridY * gridHeight + gridX] = elevation;
      this.minMax = null;
    }
  }

  @Override
  public void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, NULL_VALUE);
  }
}
