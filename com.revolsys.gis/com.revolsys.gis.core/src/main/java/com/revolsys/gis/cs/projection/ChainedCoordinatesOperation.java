package com.revolsys.gis.cs.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;

public class ChainedCoordinatesOperation implements CoordinatesOperation {
  private final List<CoordinatesOperation> operations;

  public ChainedCoordinatesOperation(
    final CoordinatesOperation... operations) {
    this(Arrays.asList(operations));
  }

  public ChainedCoordinatesOperation(
    final List<CoordinatesOperation> operations) {
    this.operations = new ArrayList<CoordinatesOperation>(operations);
  }

  public void perform(
    final Coordinates from,
    final Coordinates to) {
    Coordinates source = from;
    final Coordinates target = to;
    for (final CoordinatesOperation operation : operations) {
      operation.perform(source, target);
      source = target;
    }
  }
  @Override
  public String toString() {
    return operations.toString();
  }
}
