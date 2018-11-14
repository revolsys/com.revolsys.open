package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCsWktWriter;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.GeometryFactoryWithOffsets;
import com.revolsys.io.StringWriter;
import com.revolsys.record.code.Code;
import com.revolsys.util.Md5;

public interface CoordinateSystem extends Code, GeometryFactoryProxy, Serializable {
  public static <C extends CoordinateSystem> C getCoordinateSystem(final String wkt) {
    return EsriCoordinateSystems.readCoordinateSystem(wkt);
  }

  CoordinateSystem clone();

  boolean equalsExact(CoordinateSystem coordinateSystem);

  Area getArea();

  @Override
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
  int getCoordinateSystemId();

  String getCoordinateSystemType();

  @Override
  default String getDescription() {
    return getCoordinateSystemName();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    return getGeometryFactoryFloating(3);
  }

  GeometryFactory getGeometryFactoryFixed(int axisCount, double... scales);

  GeometryFactory getGeometryFactoryFloating(int axisCount);

  default GeometryFactory getGeometryFactoryWithOffsets(final double offsetX, final double scaleX,
    final double offsetY, final double scaleY, final double offsetZ, final double scaleZ) {
    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
      return getGeometryFactoryFixed(3, scaleX, scaleY, scaleZ);
    } else {
      return new GeometryFactoryWithOffsets(this, offsetX, scaleX, offsetY, scaleY, offsetZ,
        scaleZ);
    }
  }

  @Override
  default <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return null;
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

  LinearUnit getLinearUnit();

  <Q extends Quantity<Q>> Unit<Q> getUnit();

  boolean isDeprecated();

  boolean isSame(CoordinateSystem coordinateSystem);

  default byte[] md5Digest() {
    final MessageDigest digest = Md5.getMessageDigest();
    updateDigest(digest);
    return digest.digest();
  }

  default String toEsriWktCs() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeEsriWktCs(stringWriter, -1);
      return stringWriter.toString();
    }
  }

  default String toEsriWktCsFormatted() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeEsriWktCs(stringWriter, 0);
      return stringWriter.toString();
    }
  }

  default void updateDigest(final MessageDigest digest) {
    Md5.update(digest, toString());
  }

  default boolean writeEsriWktCs(final Writer writer, final int indentLevel) {
    final int coordinateSystemId = getCoordinateSystemId();
    final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems
      .getCoordinateSystem(coordinateSystemId);
    if (esriCoordinateSystem == null) {
      EsriCsWktWriter.write(writer, this, indentLevel);
    } else {
      EsriCsWktWriter.write(writer, esriCoordinateSystem, indentLevel);
    }
    return true;
  }
}
