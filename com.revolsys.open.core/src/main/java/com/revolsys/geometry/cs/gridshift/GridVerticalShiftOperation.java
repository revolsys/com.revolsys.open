package com.revolsys.geometry.cs.gridshift;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public interface GridVerticalShiftOperation extends CoordinatesOperation {

  @Override
  default void perform(final CoordinatesOperationPoint point) {
    verticalShift(point);
  }

  boolean verticalShift(CoordinatesOperationPoint point);
}
