package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.cs.gridshift.GridShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class GeographicCoordinateSystemGridShiftOperation implements CoordinatesOperation {
  private final GeographicCoordinateSystem sourceCoordinateSystem;

  private final GeographicCoordinateSystem targetCoordinateSystem;

  private final List<GridShiftOperation> operations = new ArrayList<>();

  public GeographicCoordinateSystemGridShiftOperation(
    final GeographicCoordinateSystem sourceCoordinateSystem,
    final GeographicCoordinateSystem targetCoordinateSystem) {
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.targetCoordinateSystem = targetCoordinateSystem;
  }

  public synchronized void addOperation(final GridShiftOperation operation) {
    if (!this.operations.contains(operation)) {
      this.operations.add(operation);
    }
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    for (final GridShiftOperation operation : this.operations) {
      if (operation.shift(point)) {
        return;
      }
    }
  }

  @Override
  public String toString() {
    return this.sourceCoordinateSystem + " -> " + this.targetCoordinateSystem;
  }

}
