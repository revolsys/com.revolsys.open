package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.record.code.Code;
import com.revolsys.util.Md5;

public interface CoordinateSystem extends Code, GeometryFactoryProxy, Serializable {
  CoordinateSystem clone();

  boolean equalsExact(CoordinateSystem coordinateSystem);

  Area getArea();

  BoundingBox getAreaBoundingBox();

  Authority getAuthority();

  List<Axis> getAxis();

  @SuppressWarnings("unchecked")
  @Override
  default <C> C getCode() {
    return (C)(Integer)getCoordinateSystemId();
  }

  CoordinatesOperation getCoordinatesOperation(CoordinateSystem coordinateSystem);

  @Override
  default CoordinateSystem getCoordinateSystem() {
    return this;
  }

  String getCoordinateSystemType();

  @Override
  default String getDescription() {
    return getCoordinateSystemName();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3d(this);
  }

  @Override
  default Integer getInteger(final int index) {
    if (index == 0) {
      return getCoordinateSystemId();
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  Unit<Length> getLengthUnit();

  <Q extends Quantity> Unit<Q> getUnit();

  boolean isDeprecated();

  default byte[] md5Digest() {
    final MessageDigest digest = Md5.getMessageDigest();
    updateDigest(digest);
    return digest.digest();
  }

  default void updateDigest(final MessageDigest digest) {
    Md5.update(digest, toString());
  }
}
