package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public interface CoordinateSystem extends Serializable {
  CoordinateSystem clone();

  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  List<Axis> getAxis();

  CoordinatesProjection getCoordinatesProjection();

  GeometryFactory getGeometryFactory();

  int getId();

  Unit<Length> getLengthUnit();

  String getName();

  <Q extends Quantity> Unit<Q> getUnit();

  boolean isDeprecated();
}
