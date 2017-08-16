package com.revolsys.elevation.gridded;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Debug;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  protected double[] bounds = BoundingBoxUtil.newBounds(3);

  private int gridHeight;

  private int gridWidth;

  private double minColourMultiple;

  private double colourGreyMultiple;

  private double gridCellSize;

  private Resource resource;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private boolean zBoundsUpdateRequired = true;

  private double scaleXY;

  public AbstractGriddedElevationModel() {
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellSize) {
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
      this.colourGreyMultiple = 1.0 / (maxZ - minZ);
      this.minColourMultiple = minZ * this.colourGreyMultiple;
      this.zBoundsUpdateRequired = false;
    }
    this.boundingBox = new BoundingBoxDoubleGf(geometryFactory, 3, this.bounds);

  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final double gridCellSize) {
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
  };

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
  public double getElevation(int gridX, int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX < 0 || gridY < 0) {
      return Double.NaN;
    } else {
      if (gridX >= width) {
        if (gridX == width) {
          gridX--;
        } else {
          return Double.NaN;
        }
      }
      if (gridY >= height) {
        if (gridY == height) {
          gridY--;
        } else {
          return Double.NaN;
        }
      }
      return getElevationDo(gridX, gridY, width);
    }
  }

  protected abstract double getElevationDo(int gridX, int gridY, int gridWidth);

  @Override
  public double getElevationFast(final int gridX, final int gridY) {
    return getElevationDo(gridX, gridY, this.gridWidth);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getGridCellSize() {
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
  public double getScaleXY() {
    return this.scaleXY;
  }

  @Override
  public boolean isEmpty() {
    getBoundingBox();
    return Double.isNaN(this.bounds[2]);
  }

  public boolean isModified() {
    return this.zBoundsUpdateRequired;
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.bounds = boundingBox.getMinMaxValues(3);
  }

  @Override
  public void setElevationsForTriangle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    final double scaleXy = this.scaleXY;
    double minX = x1;
    double maxX = x1;
    if (x2 < minX) {
      minX = x2;
    } else if (x2 > maxX) {
      maxX = x2;
    }
    if (x2 < minX) {
      minX = x2;
    } else if (x2 > maxX) {
      maxX = x2;
    }
    if (x3 < minX) {
      minX = x3;
    } else if (x3 > maxX) {
      maxX = x3;
    }

    double minY = y1;
    double maxY = y1;
    if (y2 < minY) {
      minY = y2;
    } else if (y2 > maxY) {
      maxY = y2;
    }
    if (y3 < minY) {
      minY = y3;
    } else if (y3 > maxY) {
      maxY = y3;
    }
    final double gridCellSize = this.gridCellSize;
    final double[] bounds = this.bounds;
    final double gridMinX = bounds[0];
    final double gridMaxX = bounds[3];
    final double startX;
    if (minX < gridMinX) {
      startX = gridMinX;
    } else {
      startX = Math.ceil(minX / gridCellSize) * gridCellSize;
    }
    if (maxX > gridMaxX) {
      maxX = gridMaxX;
    }
    final double gridMinY = bounds[1];
    final double gridMaxY = bounds[4];
    final double startY;
    if (minY < gridMinY) {
      startY = gridMinY;
    } else {
      startY = Math.ceil(minY / gridCellSize) * gridCellSize;
    }
    if (maxY > gridMaxY) {
      maxY = gridMaxY;
    }
    for (double y = startY; y < maxY; y += gridCellSize) {
      for (double x = startX; x < maxX; x += gridCellSize) {
        if (Triangle.containsPoint(scaleXy, x1, y1, x2, y2, x3, y3, x, y)) {
          final double elevation = Triangle.getElevation(x1, y1, z1, x2, y2, z2, x3, y3, z3, x, y);
          if (Double.isFinite(elevation)) {
            setElevation(x, y, elevation);
          }
        }
      }
    }
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.scaleXY = geometryFactory.getScaleXY();
  }

  public void setGridCellSize(final int gridCellSize) {
    this.gridCellSize = gridCellSize;
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

  public void setZBoundsUpdateRequired(final boolean zBoundsUpdateRequired) {
    this.zBoundsUpdateRequired = zBoundsUpdateRequired;
  }

  protected void setZRange(final double minZ, final double maxZ) {
    final double oldMinZ = this.bounds[2];
    if (minZ < oldMinZ || !Double.isFinite(oldMinZ)) {
      this.bounds[2] = minZ;
    }
    final double oldMaxZ = this.bounds[5];
    if (maxZ < oldMaxZ || !Double.isFinite(oldMaxZ)) {
      this.bounds[5] = maxZ;
    }
  }

  @Override
  public String toString() {
    return getBoundingBox() + " " + this.gridWidth + "x" + this.gridHeight + " c="
      + this.gridCellSize;
  }

  @Override
  public void updateZBoundingBox() {
    if (this.zBoundsUpdateRequired) {
      expandZ();
      final double minZ = this.bounds[2];
      final double maxZ = this.bounds[5];
      if (Double.isFinite(minZ)) {
        this.colourGreyMultiple = 1.0 / (maxZ - minZ);
        this.minColourMultiple = minZ * this.colourGreyMultiple;
      }
      this.boundingBox = new BoundingBoxDoubleGf(this.geometryFactory, 3, this.bounds);
      this.zBoundsUpdateRequired = false;
    }
  }
}
