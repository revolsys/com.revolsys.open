package com.revolsys.gis.cs.projection;

public abstract class AbstractCoordinatesProjection implements
  CoordinatesProjection {

  private final CoordinatesOperation inverseOperation = new InverseOperation(
    this);

  private final CoordinatesOperation projectOperation = new ProjectOperation(
    this);

  public CoordinatesOperation getInverseOperation() {
    return inverseOperation;
  }

  public CoordinatesOperation getProjectOperation() {
    return projectOperation;
  }
}
