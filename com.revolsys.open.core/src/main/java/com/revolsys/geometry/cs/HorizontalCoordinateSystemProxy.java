package com.revolsys.geometry.cs;

public interface HorizontalCoordinateSystemProxy {

  <C extends CoordinateSystem> C getHorizontalCoordinateSystem();

  default int getHorizontalCoordinateSystemId() {
    final HorizontalCoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getHorizontalCoordinateSystemId();
    }
  }

}
