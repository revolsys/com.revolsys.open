package com.revolsys.geometry.cs.projection;

public class RadiansToDegreesOperation implements CoordinatesOperation {
  public static final RadiansToDegreesOperation INSTANCE = new RadiansToDegreesOperation();

  public RadiansToDegreesOperation() {
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    point.x = Math.toDegrees(point.x);
    point.y = Math.toDegrees(point.y);
  }

}
