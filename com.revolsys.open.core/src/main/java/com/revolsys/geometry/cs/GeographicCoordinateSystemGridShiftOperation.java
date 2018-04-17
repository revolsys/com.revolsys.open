package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.cs.gridshift.GridHorizontalShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class GeographicCoordinateSystemGridShiftOperation implements CoordinatesOperation {
  private final GeographicCoordinateSystem sourceCoordinateSystem;

  private final GeographicCoordinateSystem targetCoordinateSystem;

  private final List<GridHorizontalShiftOperation> operations = new ArrayList<>();

  public GeographicCoordinateSystemGridShiftOperation(
    final GeographicCoordinateSystem sourceCoordinateSystem,
    final GeographicCoordinateSystem targetCoordinateSystem) {
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.targetCoordinateSystem = targetCoordinateSystem;
  }

  public synchronized void addOperation(final GridHorizontalShiftOperation operation) {
    if (!this.operations.contains(operation)) {
      this.operations.add(operation);
    }
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    for (final GridHorizontalShiftOperation operation : this.operations) {
      if (operation.horizontalShift(point)) {
        return;
      }
    }
  }

  public void removeOperation(final GridHorizontalShiftOperation operation) {
    this.operations.remove(operation);
  }

  @Override
  public String toString() {
    return this.sourceCoordinateSystem + " -> " + this.targetCoordinateSystem;
  }

}
