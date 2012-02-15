package com.revolsys.gis.cs;

import java.io.Serializable;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public interface CoordinateSystem extends Serializable {
  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  int getId();

  Unit<Length> getLengthUnit();

  String getName();

  <Q extends Quantity> Unit<Q> getUnit();
}
