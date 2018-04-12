package com.revolsys.geometry.cs.gridshift;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public interface GridShiftOperation extends CoordinatesOperation {

  @Override
  default void perform(final CoordinatesOperationPoint point) {
    shift(point);
  }

  boolean shift(CoordinatesOperationPoint point);
}
