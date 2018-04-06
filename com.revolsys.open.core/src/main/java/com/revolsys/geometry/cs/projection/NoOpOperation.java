package com.revolsys.geometry.cs.projection;

public class NoOpOperation implements CoordinatesOperation {

  @Override
  public void perform(CoordinatesOperationPoint point) {
     }

  @Override
  public String toString() {
    return "noOp";
  }
}
