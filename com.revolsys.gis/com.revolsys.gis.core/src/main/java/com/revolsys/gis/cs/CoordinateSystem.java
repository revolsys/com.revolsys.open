package com.revolsys.gis.cs;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public interface CoordinateSystem {
  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  Unit<Length> getLengthUnit();

  String getName();
  
  int getId();

  <Q extends Quantity> Unit<Q> getUnit();
}
