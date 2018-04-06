package com.revolsys.geometry.cs.projection;

public class InverseOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public InverseOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    this.projection.inverse(point);
    point.x = Math.toDegrees(point.x);
    point.y = Math.toDegrees(point.y);
  }

  @Override
  public String toString() {
    return this.projection + " -> geographics";
  }
}
