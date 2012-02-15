package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.Coordinates;

public interface CoordinatesOperation {
  void perform(Coordinates from, Coordinates to);
}
