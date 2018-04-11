package com.revolsys.geometry.cs.projection;

public abstract class AbstractCoordinatesProjection implements CoordinatesProjection {

  private final CoordinatesOperation inverseOperation = this::inverse;

  private final CoordinatesOperation projectOperation = this::project;

  @Override
  public CoordinatesOperation getInverseOperation() {
    return this.inverseOperation;
  }

  @Override
  public CoordinatesOperation getProjectOperation() {
    return this.projectOperation;
  }
}
