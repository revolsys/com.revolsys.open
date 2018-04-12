package com.revolsys.geometry.cs.gridshift.nadcon5;

import com.revolsys.geometry.cs.gridshift.GridShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class Nadcon5GridShiftOperation implements GridShiftOperation {

  private final String sourceDatumName;

  private final String targetDatumName;

  public Nadcon5GridShiftOperation(final String sourceDatumName, final String targetDatumName) {
    this.sourceDatumName = sourceDatumName;
    this.targetDatumName = targetDatumName;
  }

  @Override
  public boolean shift(final CoordinatesOperationPoint point) {
    double lon = point.x;
    if (lon < 0) {
      lon += 360;
    }
    final double lat = point.y;
    final Nadcon5Region region = Nadcon5Region.getRegion(lon, lat);
    if (region == null) {
      return false;
    } else {
      final int sourceDatumIndex = region.getDatumIndex(this.sourceDatumName);
      final int targetDatumIndex = region.getDatumIndex(this.targetDatumName);

      double newLon = lon;
      double newLat = lat;
      if (targetDatumIndex > sourceDatumIndex) {
        for (int i = sourceDatumIndex; i < targetDatumIndex; ++i) {
          final int fileIndex = i;
          final double lonShift = region.getLonShift(fileIndex, newLon, newLat);
          final double latShift = region.getLatShift(fileIndex, newLon, newLat);
          if (Double.isFinite(lonShift)) {
            newLon += lonShift;
            newLat += latShift;
            if (!region.covers(newLon, newLat)) {
              return false;
            }
          } else {
            return false;
          }
        }
      } else {
        for (int j = sourceDatumIndex; j > targetDatumIndex; --j) {
          final int fileIndex = j - 1;
          final double lonShift = region.getLonShift(fileIndex, newLon, newLat);
          final double latShift = region.getLatShift(fileIndex, newLon, newLat);
          if (Double.isFinite(lonShift)) {
            newLon -= lonShift;
            newLat -= latShift;
            if (!region.covers(newLon, newLat)) {
              return false;
            }
          } else {
            return false;
          }
        }
      }
      if (newLon > 180) {
        point.x = newLon - 360;
      } else {
        point.x = newLon;
      }
      point.y = newLat;
      return true;
    }
  }

  @Override
  public String toString() {
    return "Nadcon5: " + this.sourceDatumName + " -> " + this.targetDatumName;
  }
}
