package com.revolsys.gis.cs.projection;


public interface CoordinatesOperation {

  void perform(int sourceAxisCount, double[] sourceCoordinates,
    int targetAxisCount, double[] targetCoordinates);
}
