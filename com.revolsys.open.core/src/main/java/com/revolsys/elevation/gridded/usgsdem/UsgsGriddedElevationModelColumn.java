package com.revolsys.elevation.gridded.usgsdem;

public class UsgsGriddedElevationModelColumn {
  private final int gridY;

  final int[] elevations;

  public UsgsGriddedElevationModelColumn(final int gridY, final int[] elevations) {
    this.gridY = gridY;
    this.elevations = elevations;
  }

  public int getElevationInt(final int gridY) {
    final int index = gridY - this.gridY;
    if (index >= 0 && index < this.elevations.length) {
      return this.elevations[index];
    } else {
      return Integer.MIN_VALUE;
    }
  }

  public boolean hasElevation(final int gridY) {
    final int index = gridY - this.gridY;
    if (index >= 0 && index < this.elevations.length) {
      return this.elevations[index] != Integer.MIN_VALUE;
    } else {
      return false;
    }
  }
}
