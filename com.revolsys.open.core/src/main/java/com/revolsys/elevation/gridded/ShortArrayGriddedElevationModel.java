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
  public BufferedImage getBufferedImage() {
    final ShortMinMax minMax = getMinMax();
    final short min = minMax.getMin();
    final int range = minMax.getRange();
    final float multiple = 255f / range;
    final int width = getGridWidth();
    final int height = getGridHeight();
    int i = 0;
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    // Images are from top left as opposed to bottom left
    for (int y = height - 1; y >= 0; y--) {
      for (int x = 0; x < width; x++) {
        final short elevation = this.elevations[i];
        if (Doubles.equal(elevation, NULL_VALUE)) {
          image.setRGB(x, y, WebColors.colorToRGB(0, 0, 0, 0));
        } else {
          final int grey = Math.round((elevation - min) * multiple);
          final int color = WebColors.colorToRGB(255, grey, grey, grey);
          image.setRGB(x, y, color);
        }
        i++;
      }
    }
    return image;
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
