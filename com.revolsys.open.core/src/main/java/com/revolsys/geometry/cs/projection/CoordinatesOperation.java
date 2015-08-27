package com.revolsys.geometry.cs.projection;

public interface CoordinatesOperation {

  void perform(int sourceAxisCount, double[] sourceCoordinates, int targetAxisCount,
    double[] targetCoordinates);
}
