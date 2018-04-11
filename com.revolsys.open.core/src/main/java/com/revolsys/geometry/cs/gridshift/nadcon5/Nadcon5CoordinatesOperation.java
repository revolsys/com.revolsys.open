package com.revolsys.geometry.cs.gridshift.nadcon5;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class Nadcon5CoordinatesOperation implements CoordinatesOperation {

  public static final CoordinatesOperation NAD_27_83 = new Nadcon5CoordinatesOperation(
    Nadcon5Region.NAD27, Nadcon5Region.NAD83_CURRENT);

  public static final CoordinatesOperation NAD_83_28 = new Nadcon5CoordinatesOperation(
    Nadcon5Region.NAD83_CURRENT, Nadcon5Region.NAD27);

  private final String sourceDatumName;

  private final String targetDatumName;

  public Nadcon5CoordinatesOperation(final String sourceDatumName, final String targetDatumName) {
    this.sourceDatumName = sourceDatumName;
    this.targetDatumName = targetDatumName;
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    double lon = point.x;
    if (lon < 0) {
      lon += 360;
    }
    final double lat = point.y;
    final Nadcon5Region region = Nadcon5Region.getRegion(lon, lat);
    if (region == null) {
      throw new IllegalArgumentException("No suitable regionName found for datum transformation");
    } else {
      final int sourceDatumIndex = region.getDatumIndex(this.sourceDatumName);
      final int targetDatumIndex = region.getDatumIndex(this.targetDatumName);

      double newLat = lat;
      double newLon = lon;
      if (targetDatumIndex > sourceDatumIndex) {
        for (int i = sourceDatumIndex; i < targetDatumIndex; ++i) {
          final int fileIndex = i;
          final double lonShift = region.getLonShift(fileIndex, newLon, newLat);
          final double latShift = region.getLatShift(fileIndex, newLon, newLat);
          if (Double.isFinite(lonShift)) {
            newLat += latShift;
            newLon += lonShift;
            if (!region.intersectsBounds(newLon, newLat)) {
              throw new IllegalArgumentException(
                "Transformation failure;coordinate is out of bounds");
            }
          } else {
            throw new IllegalArgumentException("Transformation failure;no grids found");
          }
        }
      } else {
        for (int j = sourceDatumIndex; j > targetDatumIndex; --j) {
          final int fileIndex = j - 1;
          final double lonShift = region.getLonShift(fileIndex, newLon, newLat);
          final double latShift = region.getLatShift(fileIndex, newLon, newLat);
          if (Double.isFinite(lonShift)) {
            newLat -= latShift;
            newLon -= lonShift;
            if (!region.intersectsBounds(newLon, newLat)) {
              throw new IllegalArgumentException(
                "Transformation failure;coordinate is out of bounds");
            }
          } else {
            throw new IllegalArgumentException("Transformation failure;no grids found");
          }
        }
      }
      if (newLon > 180) {
        point.x = newLon - 360;
      } else {
        point.x = newLon;
      }
      point.y = newLat;
    }
  }
}
