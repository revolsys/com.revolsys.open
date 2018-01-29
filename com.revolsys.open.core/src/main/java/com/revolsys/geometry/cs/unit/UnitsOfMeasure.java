package com.revolsys.geometry.cs.unit;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;

public interface UnitsOfMeasure {
  static Degree DEGREE = EpsgCoordinateSystems.getUnit(9102);

  static Metre METRE = EpsgCoordinateSystems.getUnit(9001);
}
