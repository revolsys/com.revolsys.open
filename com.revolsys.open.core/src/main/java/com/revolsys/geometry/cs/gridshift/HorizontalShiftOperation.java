package com.revolsys.geometry.cs.gridshift;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public interface HorizontalShiftOperation extends CoordinatesOperation {

  boolean horizontalShift(CoordinatesOperationPoint point);

  @Override
  default void perform(final CoordinatesOperationPoint point) {
    horizontalShift(point);
  }
}
