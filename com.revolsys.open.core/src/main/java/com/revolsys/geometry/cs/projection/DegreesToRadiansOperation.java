package com.revolsys.geometry.cs.projection;

public class DegreesToRadiansOperation implements CoordinatesOperation {
  public static final DegreesToRadiansOperation INSTANCE = new DegreesToRadiansOperation();

  public DegreesToRadiansOperation() {
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
  }

}
