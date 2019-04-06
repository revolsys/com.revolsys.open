package com.revolsys.core.test.geometry.test.model;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.datatype.DataType;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
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
    if (!geometryFactory.isHasHorizontalCoordinateSystem() || geometryFactory.isGeographics()) {
      x = -123.123456;
      y = 52.123456;
    } else {
      final BoundingBox areaBoundingBox = geometryFactory.getAreaBoundingBox();
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

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G geometry(final GeometryFactory geometryFactory,
    final DataType geometryType, final int axisCount, final int partCount, final int ringCount) {
    if (GeometryDataTypes.POINT.equals(geometryType)) {
      return (G)point(geometryFactory, axisCount);
    } else if (GeometryDataTypes.MULTI_POINT.equals(geometryType)) {
      return (G)multiPoint(geometryFactory, axisCount, partCount);
    } else if (GeometryDataTypes.LINE_STRING.equals(geometryType)) {
      return (G)lineString(geometryFactory, axisCount);
    } else if (GeometryDataTypes.MULTI_LINE_STRING.equals(geometryType)) {
      return (G)multiLineString(geometryFactory, axisCount, partCount);
    } else if (GeometryDataTypes.POLYGON.equals(geometryType)) {
      return (G)polygon(geometryFactory, axisCount, ringCount);
    } else if (GeometryDataTypes.MULTI_POLYGON.equals(geometryType)) {
      return (G)multiPolygon(geometryFactory, axisCount, partCount, ringCount);
    } else {
      return null;
    }
  }

  public static Geometry geometry(final GeometryFactory geometryFactory,
    final DataType geometryDataType, final int geometryCount, final int ringCount,
    final int vertexCount, final double delta) {
    if (GeometryDataTypes.POINT.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.point();
      } else {
        return point(geometryFactory, delta);
      }
    } else if (GeometryDataTypes.MULTI_POINT.equals(geometryDataType)) {
      return multiPoint(geometryFactory, geometryCount, delta);
    } else if (GeometryDataTypes.LINE_STRING.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.lineString();
      } else {
        return lineString(geometryFactory, vertexCount, delta);
      }
    } else if (GeometryDataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      return multiLineString(geometryFactory, geometryCount, vertexCount, delta);
    } else if (GeometryDataTypes.POLYGON.equals(geometryDataType)) {
      if (geometryCount == 0) {
        return geometryFactory.polygon();
      } else {
        return polygon(geometryFactory, ringCount, delta);
      }
    } else if (GeometryDataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      return multiPolygon(geometryFactory, ringCount, vertexCount, delta);
    } else {
      throw new IllegalArgumentException("Cannot create " + geometryDataType);
    }

  }

  private static Point getCentre(final GeometryFactory geometryFactory) {
    final BoundingBox areaBoundingBox = geometryFactory.getAreaBoundingBox();
    final Point centre = areaBoundingBox.getCentre();
    return centre;
  }

  public static LinearRing linearRing(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex, final int ringIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount * 5];
    int offset = 0;
    int x1 = partIndex * 20;
    int y1 = 0;
    int x2;
    int y2;
    if (ringIndex == 0) {
      x2 = x1 + 19;
      y2 = y1 + 10;
    } else {
      x1 += ringIndex * 2;
      x2 = x1 + 1;
      y1 += 1;
      y2 = y1 + 8;
    }

    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, x1, y1, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, x2, y1, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, x2, y2, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, x1, y2, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, x1, y1, centre);
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  public static LineString lineString(final GeometryFactory geometryFactory, final int axisCount) {
    return lineString(geometryFactory, axisCount, 0);
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

  public static LineString lineString(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount * 2];
    int offset = 0;
    offset = setPoint(coordinates, offset, geometryFactory, axisCount, partIndex * 2, centre);
    offset = setPoint(coordinates, offset, geometryFactory, axisCount, partIndex * 2 + 1, centre);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  public static Lineal multiLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int partCount) {
    final LineString[] lines = new LineString[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      lines[partIndex] = lineString(geometryFactory, axisCount, partIndex);
    }
    return geometryFactory.lineal(lines);
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

  public static Punctual multiPoint(final GeometryFactory geometryFactory, final int axisCount,
    final int partCount) {
    final Point[] points = new Point[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      points[partIndex] = point(geometryFactory, axisCount, partIndex);
    }
    return geometryFactory.punctual(points);
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

  public static Polygonal multiPolygon(final GeometryFactory geometryFactory, final int axisCount,
    final int partCount, final int ringCount) {
    final Polygon[] polygons = new Polygon[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      polygons[partIndex] = polygon(geometryFactory, axisCount, partIndex, ringCount);
    }
    final Polygonal multiPolygon = geometryFactory.polygonal(polygons);
    return multiPolygon;
  }

  public static Point point(final GeometryFactory geometryFactory, final double delta) {
    final double[] coordinates = coordinates(geometryFactory, delta);
    return geometryFactory.point(coordinates);
  }

  public static Point point(final GeometryFactory geometryFactory, final int axisCount) {
    return point(geometryFactory, axisCount, 0);
  }

  public static Point point(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount];
    setPoint(coordinates, 0, geometryFactory, axisCount, partIndex, centre);
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
      final double x = Doubles.makePrecise(1000000, firstPoint[0] + offset);
      final double y = Doubles.makePrecise(1000000, firstPoint[1] + offset);

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
        point[0] = Doubles.makePrecise(1000000, point[0]);
        point[1] = Doubles.makePrecise(1000000, point[1]);

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

  public static Polygon polygon(final GeometryFactory geometryFactory, final int axisCount,
    final int ringCount) {
    return polygon(geometryFactory, axisCount, 0, ringCount);
  }

  public static Polygon polygon(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex, final int ringCount) {
    final LinearRing[] rings = new LinearRing[ringCount];
    for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
      rings[ringIndex] = linearRing(geometryFactory, axisCount, partIndex, ringIndex);
    }
    return geometryFactory.polygon(rings);
  }

  private static int setPoint(final double[] coordinates, int offset,
    final GeometryFactory geometryFactory, final int axisCount, final int partIndex,
    final Point centre) {
    coordinates[offset++] = centre.getX() + partIndex * 1;
    coordinates[offset++] = Doubles.makePrecise(1000000, centre.getY());
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      coordinates[offset++] = axisIndex * 10 + resolution;
    }
    return offset;
  }

  private static int setRingPoint(final double[] coordinates, int offset,
    final GeometryFactory geometryFactory, final int axisCount, final int xIndex, final int yIndex,
    final Point centre) {
    coordinates[offset++] = centre.getX() + xIndex * 1;
    coordinates[offset++] = Doubles.makePrecise(1000000, centre.getY()) + yIndex * 1;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      coordinates[offset++] = axisIndex * 10 + resolution;
    }
    return offset;
  }

}
