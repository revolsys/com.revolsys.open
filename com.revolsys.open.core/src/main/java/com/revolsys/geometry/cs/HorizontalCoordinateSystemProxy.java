package com.revolsys.geometry.cs;

import com.revolsys.geometry.cs.gridshift.GridHorizontalShiftOperation;

public interface HorizontalCoordinateSystemProxy {
  default void addGridShiftOperation(final HorizontalCoordinateSystemProxy coordinateSystem,
    final GridHorizontalShiftOperation operation) {
    final GeographicCoordinateSystem sourceCs = getHorizontalCoordinateSystem();
    final GeographicCoordinateSystem targetCs = coordinateSystem.getHorizontalCoordinateSystem();
    if (sourceCs == null) {
      throw new IllegalArgumentException(this + " does not have a coordinate system");
    } else if (targetCs == null) {
      throw new IllegalArgumentException(targetCs + " does not have a coordinate system");
    } else {
      sourceCs.addGridShiftOperation(targetCs, operation);
    }
  }

  <C extends CoordinateSystem> C getHorizontalCoordinateSystem();

  default int getHorizontalCoordinateSystemId() {
    final HorizontalCoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getHorizontalCoordinateSystemId();
    }
  }

  default void removeGridShiftOperation(final HorizontalCoordinateSystemProxy coordinateSystem,
    final GridHorizontalShiftOperation operation) {
    final GeographicCoordinateSystem sourceCs = getHorizontalCoordinateSystem();
    final GeographicCoordinateSystem targetCs = coordinateSystem.getHorizontalCoordinateSystem();
    if (sourceCs == null) {
      throw new IllegalArgumentException(this + " does not have a coordinate system");
    } else if (targetCs == null) {
      throw new IllegalArgumentException(targetCs + " does not have a coordinate system");
    } else {
      sourceCs.removeGridShiftOperation(targetCs, operation);
    }
  }

}
