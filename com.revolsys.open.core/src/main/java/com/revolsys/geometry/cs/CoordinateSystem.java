package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;

public interface CoordinateSystem extends GeometryFactoryProxy, Serializable {
  CoordinateSystem clone();

  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  List<Axis> getAxis();

  CoordinatesProjection getCoordinatesProjection();

  @Override
  default CoordinateSystem getCoordinateSystem() {
    return this;
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3(this);
  }

  Unit<Length> getLengthUnit();

  <Q extends Quantity<Q>> Unit<Q> getUnit();

  boolean isDeprecated();
}
