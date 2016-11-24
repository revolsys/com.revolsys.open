package com.revolsys.elevation.gridded;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  private double[] bounds = BoundingBoxUtil.newBounds(3);

  private int gridHeight;

  private int gridWidth;

  private double minColourMultiple;

  private double colourGreyMultiple;

  private int gridCellSize;

  private Resource resource;

  private GriddedElevationModelImage image;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private boolean zBoundsUpdateRequired = true;

  public AbstractGriddedElevationModel() {
  };

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final int gridCellSize) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellSize = gridCellSize;
    setGeometryFactory(geometryFactory);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double minZ = boundingBox.getMinZ();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    final double maxZ = boundingBox.getMaxZ();
    this.bounds = new double[] {
      minX, minY, minZ, maxX, maxY, maxZ
    };
    if (Double.isFinite(minZ)) {
      this.colourGreyMultiple = 1.0f / (maxZ - minZ);
      this.minColourMultiple = minZ * this.colourGreyMultiple;
      this.zBoundsUpdateRequired = false;
    }
    this.boundingBox = new BoundingBoxDoubleGf(geometryFactory, 3, this.bounds);

  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final int gridCellSize) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellSize = gridCellSize;
    setGeometryFactory(geometryFactory);
    this.bounds = new double[] {
      minX, minY, Double.NaN, minX + gridWidth * gridCellSize, minY + gridHeight * gridCellSize,
      Double.NaN
    };
    this.boundingBox = new BoundingBoxDoubleGf(geometryFactory, 3, this.bounds);
  }

  @Override
  public void clear() {
    clearCachedObjects();
  }

  protected void clearCachedObjects() {
    this.zBoundsUpdateRequired = true;
  }

  protected void expandZ() {
    this.bounds[2] = Double.NaN;
    this.bounds[5] = Double.NaN;
    final int gridWidth = this.gridWidth;
    final int gridHeight = this.gridHeight;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double elevation = getElevation(gridX, gridY);
        if (Double.isFinite(elevation)) {
          expandZ(elevation);
        }
      }
    }

  }

  protected void expandZ(final double elevation) {
    if (Double.isFinite(elevation)) {
      final double minZ = this.bounds[2];
      if (elevation < minZ || !Double.isFinite(minZ)) {
        this.bounds[2] = elevation;
      }
      final double maxZ = this.bounds[5];
      if (elevation > maxZ || !Double.isFinite(maxZ)) {
        this.bounds[5] = elevation;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    updateZBoundingBox();
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
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int getGridCellSize() {
    return this.gridCellSize;
  }

  @Override
  public int getGridCellX(final double x) {
    final double minX = this.bounds[0];
    final double deltaX = x - minX;
    final double cellDiv = deltaX / this.gridCellSize;
    final int gridX = (int)Math.floor(cellDiv);
    return gridX;
  }

  @Override
  public int getGridCellY(final double y) {
    final double minY = this.bounds[1];
    final double deltaY = y - minY;
    final double cellDiv = deltaY / this.gridCellSize;
    final int gridY = (int)Math.floor(cellDiv);
    return gridY;
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
  public double getMaxX() {
    return this.bounds[3];
  }

  @Override
  public double getMaxY() {
    return this.bounds[4];
  }

  @Override
  public double getMaxZ() {
    updateZBoundingBox();
    return this.bounds[5];
  }

  @Override
  public double getMinX() {
    return this.bounds[0];
  }

  @Override
  public double getMinY() {
    return this.bounds[1];
  }

  @Override
  public double getMinZ() {
    updateZBoundingBox();
    return this.bounds[2];
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public boolean isEmpty() {
    getBoundingBox();
    return Double.isNaN(this.bounds[2]);
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.bounds = boundingBox.getMinMaxValues(3);
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

  private void updateZBoundingBox() {
    if (this.zBoundsUpdateRequired) {
      expandZ();
      final double minZ = this.bounds[2];
      final double maxZ = this.bounds[5];
      if (Double.isFinite(minZ)) {
        this.colourGreyMultiple = 1.0f / (maxZ - minZ);
        this.minColourMultiple = minZ * this.colourGreyMultiple;
      }
      this.boundingBox = new BoundingBoxDoubleGf(this.geometryFactory, 3, this.bounds);
      this.zBoundsUpdateRequired = false;
    }
  }
}
