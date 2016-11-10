package com.revolsys.elevation.gridded;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.range.DoubleMinMax;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  private BoundingBox boundingBox;

  private int gridHeight;

  private int gridWidth;

  private DoubleMinMax minMax;

  private double minColourMultiple;

  private double colourGreyMultiple;

  private int gridCellSize;

  private Resource resource;

  private GriddedElevationModelImage image;

  private double minX;

  private double minY;

  private GeometryFactory geometryFactory;

  public AbstractGriddedElevationModel() {
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final int gridCellSize) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellSize = gridCellSize;
    this.geometryFactory = geometryFactory;
    this.minX = minX;
    this.minY = minY;
  }

  @Override
  public void clear() {
    clearCachedObjects();
  }

  protected void clearCachedObjects() {
    this.minMax = null;
  }

  protected void expandMinMax(final DoubleMinMax minMax) {
    for (int i = 0; i < this.gridWidth; i++) {
      for (int j = 0; j < this.gridWidth; j++) {
        final double elevation = getElevation(i, j);
        if (Double.isFinite(elevation)) {
          minMax.add(elevation);
        }
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      final double maxX = this.minX + (double)this.gridWidth * this.gridCellSize;
      final double maxY = this.minY + (double)this.gridHeight * this.gridCellSize;
      final double x1 = this.minX;
      final double y1 = this.minY;
      this.boundingBox = this.geometryFactory.newBoundingBox(x1, y1, maxX, maxY);
    }
    return this.boundingBox;
  }

  @Override
  public int getColour(final int gridX, final int gridY) {
    final int colour;
    final double elevation = getElevation(gridX, gridY);
    if (Double.isNaN(elevation)) {
      colour = NULL_COLOUR;
    } else {
      final double elevationMultiple = elevation * this.colourGreyMultiple;
      final double elevationPercent = elevationMultiple - this.minColourMultiple;
      final int grey = (int)Math.round(elevationPercent * 255);
      colour = WebColors.colorToRGB(255, grey, grey, grey);
    }
    return colour;
  }

  @Override
  public int getGridCellSize() {
    return this.gridCellSize;
  }

  @Override
  public int getGridHeight() {
    return this.gridHeight;
  }

  @Override
  public int getGridWidth() {
    return this.gridWidth;
  }

  @Override
  public GriddedElevationModelImage getImage() {
    if (this.image == null) {
      this.image = new GriddedElevationModelImage(this);
    }
    return this.image;
  }

  @Override
  public DoubleMinMax getMinMax() {
    if (this.minMax == null) {
      this.minMax = new DoubleMinMax();
      expandMinMax(this.minMax);
      final double minZ = this.minMax.getMin();
      this.colourGreyMultiple = 1.0f / this.minMax.getRange();
      this.minColourMultiple = minZ * this.colourGreyMultiple;
    }
    return this.minMax;
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public boolean isEmpty() {
    final DoubleMinMax minMax = getMinMax();
    return minMax.isEmpty();
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setGridHeight(final int gridHeight) {
    this.gridHeight = gridHeight;
  }

  public void setGridWidth(final int gridWidth) {
    this.gridWidth = gridWidth;
  }

  @Override
  public void setResource(final Resource resource) {
    this.resource = resource;
  }
}
