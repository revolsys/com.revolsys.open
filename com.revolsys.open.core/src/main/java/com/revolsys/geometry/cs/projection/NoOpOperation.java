package com.revolsys.geometry.cs.projection;

public class NoOpOperation implements CoordinatesOperation {

  public static final NoOpOperation INSTANCE = new NoOpOperation();

  private NoOpOperation() {
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
  }

  @Override
  public String toString() {
    return "noOp";
  }
}
