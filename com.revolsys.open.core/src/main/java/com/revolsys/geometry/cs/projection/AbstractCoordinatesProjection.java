package com.revolsys.geometry.cs.projection;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;

public abstract class AbstractCoordinatesProjection implements CoordinatesProjection {

  private final CoordinatesOperation inverseOperation = new InverseOperation(this);

  private final CoordinatesOperation projectOperation = new ProjectOperation(this);

  @Override
  public CoordinatesOperation getInverseOperation() {
    return this.inverseOperation;
  }

  @Override
  public CoordinatesOperation getProjectOperation() {
    return this.projectOperation;
  }
}
