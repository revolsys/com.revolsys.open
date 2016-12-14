package com.revolsys.geometry.test.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;

public class GeometryTestUtil {

  public static double[] coordinates(final GeometryFactory geometryFactory, final double delta) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = new double[axisCount];
    double x;
    double y;
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null || geometryFactory.isGeographics()) {
      x = -123.123456;
      y = 52.123456;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      x = Math.round(areaBoundingBox.getCentreX());
      y = Math.round(areaBoundingBox.getCentreY());
    }

    coordinates[0] = x;
    coordinates[1] = y;
    if (axisCount > 2) {
      coordinates[2] = 2.1234567;
    }
    if (axisCount > 3) {
      coordinates[3] = 3.1234567;
    }
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] += delta;
    }
    return coordinates;
  }

  public static Geometry geometry(final GeometryFactory geometryFactory,
    final DataType geometryDataType, final int geometryCount, final int ringCount,
    final int vertexCount, final double delta) {
    if (DataTypes.POINT.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.point();
      } else {
        return point(geometryFactory, delta);
      }
    } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
      return multiPoint(geometryFactory, geometryCount, delta);
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.lineString();
      } else {
        return lineString(geometryFactory, vertexCount, delta);
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      return multiLineString(geometryFactory, geometryCount, vertexCount, delta);
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.polygon();
      } else {
        return polygon(geometryFactory, ringCount, delta);
      }
    } else if (DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      return multiPolygon(geometryFactory, ringCount, vertexCount, delta);
    } else {
      throw new IllegalArgumentException("Cannot create " + geometryDataType);
    }

  }

  public static LineString lineString(final GeometryFactory geometryFactory, final int vertexCount,
    final double delta) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = new double[axisCount * vertexCount];
    for (int i = 0; i < vertexCount; i++) {
      final double[] point = coordinates(geometryFactory, delta);
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, i, point);
    }
    return geometryFactory.lineString(axisCount, coordinates);
  }

  public static Lineal multiLineString(final GeometryFactory geometryFactory,
    final int geometryCount, final int vertexCount, final double delta) {
    final List<Geometry> geometries = new ArrayList<>();
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = lineString(geometryFactory, vertexCount + i,
        delta * (vertexCount + i * 3));
      geometries.add(geometry);
    }
    return geometryFactory.lineal(geometries);
  }

  public static Punctual multiPoint(final GeometryFactory geometryFactory, final int geometryCount,
    final double delta) {
    final List<Geometry> geometries = new ArrayList<>();
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = point(geometryFactory, delta * i);
      geometries.add(geometry);
    }
    final Punctual multiPoint = geometryFactory.punctual(geometries);
    return multiPoint;
  }

  public static Polygonal multiPolygon(final GeometryFactory geometryFactory,
    final int geometryCount, final int ringCount, final double delta) {
    final List<Geometry> geometries = new ArrayList<>();
    for (int i = 0; i < geometryCount; i++) {
      final Geometry geometry = polygon(geometryFactory, ringCount, delta * (i + 1));
      geometries.add(geometry);
    }
    final Polygonal multiGeometry = geometryFactory.polygonal(geometries);
    return multiGeometry;
  }

  public static Point point(final GeometryFactory geometryFactory, final double delta) {
    final double[] coordinates = coordinates(geometryFactory, delta);
    return geometryFactory.point(coordinates);
  }

  public static Polygon polygon(final GeometryFactory geometryFactory, final int ringCount,
    final double delta) {
    final int axisCount = geometryFactory.getAxisCount();
    final List<LinearRing> rings = new ArrayList<>();
    for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
      final double[] coordinates = new double[axisCount * 5];

      final double offset = delta / 100 * ringIndex;
      final double size = delta - offset * 2;

      final double[] firstPoint = coordinates(geometryFactory, delta);
      final double x = firstPoint[0] + offset;
      final double y = firstPoint[1] + offset;

      for (int vertexIndex = 0; vertexIndex < 5; vertexIndex++) {
        final double[] point = coordinates(geometryFactory, delta);
        point[0] = x;
        point[1] = y;
        if (vertexIndex == 1) {
          point[1] += size;
        } else if (vertexIndex == 2) {
          point[0] += size;
          point[1] += size;
        } else if (vertexIndex == 3) {
          point[0] += size;
        }
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex, point);
      }
      LinearRing ring = geometryFactory.linearRing(axisCount, coordinates);
      if (ringIndex > 0) {
        ring = ring.reverse();
      }
      rings.add(ring);
    }

    final Polygon polygon = geometryFactory.polygon(rings);
    return polygon;
  }

}
