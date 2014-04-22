package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public interface CoordinatesOperation {
  void perform(Coordinates from, Coordinates to);

  void perform(int sourceAxisCount, double[] sourceCoordinates,
    int targetAxisCount, double[] targetCoordinates);
}
