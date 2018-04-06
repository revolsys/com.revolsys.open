package com.revolsys.geometry.cs.gsb;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class BindaryGridShiftGrid extends BoundingBoxDoubleXY {
  private static final long serialVersionUID = 1L;

  private final List<BindaryGridShiftGrid> grids = new ArrayList<>();

  private final FloatArrayGriddedElevationModel latAccuracies;

  private final FloatArrayGriddedElevationModel latShifts;

  private final FloatArrayGriddedElevationModel lonAccuracies;

  private final FloatArrayGriddedElevationModel lonShifts;

  private final String name;

  private final String parentName;

  @SuppressWarnings("unused")
  public BindaryGridShiftGrid(final BindaryGridShiftFile file, final boolean loadAccuracy) {
    this.name = file.readRecordString();
    this.parentName = file.readRecordString();
    final String created = file.readRecordString();
    final String updated = file.readRecordString();
    this.minY = file.readRecordDouble();
    this.maxY = file.readRecordDouble();
    this.minX = file.readRecordDouble();
    this.maxX = file.readRecordDouble();
    final double gridCellSizeY = file.readRecordDouble();
    final double gridCellSizeX = file.readRecordDouble();
    if (gridCellSizeX != gridCellSizeY) {
      throw new IllegalStateException(
        "latInterval=" + gridCellSizeY + " != lonInterval=" + gridCellSizeX);
    }
    final int gridWidth = 1 + (int)((this.maxX - this.minX) / gridCellSizeY);
    final int gridHeight = 1 + (int)((this.maxY - this.minY) / gridCellSizeX);
    final int nodeCount = file.readRecordInt();
    if (nodeCount != gridWidth * gridHeight) {
      throw new IllegalStateException(
        "BindaryGridShiftGrid " + this.name + " has inconsistent grid dimensions");
    }
    final float[] latShifts = new float[nodeCount];
    final float[] lonShifts = new float[nodeCount];
    if (loadAccuracy) {
      final float[] latAccuracies = new float[nodeCount];
      final float[] lonAccuracies = new float[nodeCount];
      for (int i = 0; i < nodeCount; i++) {
        latShifts[i] = file.readFloat();
        lonShifts[i] = file.readFloat();
        latAccuracies[i] = file.readFloat();
        lonAccuracies[i] = file.readFloat();
      }
      this.lonAccuracies = new FloatArrayGriddedElevationModel(this.minX, this.minY, gridWidth,
        gridHeight, gridCellSizeY, lonAccuracies);
      this.latAccuracies = new FloatArrayGriddedElevationModel(this.minX, this.minY, gridWidth,
        gridHeight, gridCellSizeY, latAccuracies);
    } else {
      for (int i = 0; i < nodeCount; i++) {
        latShifts[i] = file.readFloat();
        lonShifts[i] = file.readFloat();
        final float latAccuracy = file.readFloat();
        final float lonAccuracy = file.readFloat();
      }
      this.lonAccuracies = null;
      this.latAccuracies = null;
    }
    this.lonShifts = new FloatArrayGriddedElevationModel(this.minX, this.minY, gridWidth,
      gridHeight, gridCellSizeX, lonShifts);
    this.latShifts = new FloatArrayGriddedElevationModel(this.minX, this.minY, gridWidth,
      gridHeight, gridCellSizeX, latShifts);
  }

  public void addGrid(final BindaryGridShiftGrid grid) {
    this.grids.add(grid);
  }

  public BindaryGridShiftGrid getGrid(final double lon, final double lat) {
    if (covers(lon, lat)) {
      for (final BindaryGridShiftGrid grid : this.grids) {
        final BindaryGridShiftGrid childGrid = grid.getGrid(lon, lat);
        if (childGrid != null) {
          return childGrid;
        }
      }
      return this;
    } else {
      return null;
    }
  }

  public double getLatAccuracy(final double lon, final double lat) {
    return this.latAccuracies.getElevationBilinear(lon, lat);
  }

  public double getLatShift(final double lon, final double lat) {
    return this.latShifts.getElevationBilinear(lon, lat);
  }

  public double getLonAccuracy(final double lon, final double lat) {
    return this.lonAccuracies.getElevationBilinear(lon, lat);
  }

  public double getLonShift(final double lon, final double lat) {
    return this.lonShifts.getElevationBilinear(lon, lat);
  }

  public String getName() {
    return this.name;
  }

  public String getParentName() {
    return this.parentName;
  }

  public boolean hasParent() {
    return !this.parentName.equalsIgnoreCase("NONE");
  }

  @Override
  public String toString() {
    return this.name;
  }

}
