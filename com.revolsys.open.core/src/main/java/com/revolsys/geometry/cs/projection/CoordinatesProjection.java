package com.revolsys.geometry.cs.projection;

public interface CoordinatesProjection {
  CoordinatesOperation getInverseOperation();

  CoordinatesOperation getProjectOperation();

  void inverse(CoordinatesOperationPoint point);

  void project(CoordinatesOperationPoint point);
}
