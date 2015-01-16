package com.revolsys.gis.cs.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChainedCoordinatesOperation implements CoordinatesOperation {
  private final List<CoordinatesOperation> operations;

  public ChainedCoordinatesOperation(final CoordinatesOperation... operations) {
    this(Arrays.asList(operations));
  }

  public ChainedCoordinatesOperation(final List<CoordinatesOperation> operations) {
    this.operations = new ArrayList<CoordinatesOperation>(operations);
  }

  @Override
  public void perform(int sourceAxisCount, double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    for (final CoordinatesOperation operation : this.operations) {
      operation.perform(sourceAxisCount, sourceCoordinates, targetAxisCount,
        targetCoordinates);
      sourceAxisCount = targetAxisCount;
      sourceCoordinates = targetCoordinates;
    }
  }

  @Override
  public String toString() {
    return this.operations.toString();
  }
}
