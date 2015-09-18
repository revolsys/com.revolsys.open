package com.revolsys.gis.postgresql.type;

import java.io.InputStream;

import org.postgresql.util.PGobject;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public class PostgreSQLGeometryWrapper extends PGobject {

  private static final long serialVersionUID = 0L;

  public PostgreSQLGeometryWrapper() {
    setType("geometry");
  }

  public PostgreSQLGeometryWrapper(final Geometry geometry) {
    this();
    setGeometry(geometry);
  }

  @Override
  public PostgreSQLGeometryWrapper clone() {
    try {
      return (PostgreSQLGeometryWrapper)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public Geometry getGeometry() {
    final String value = getValue().trim();

    int srid = -1;
    String wkt;
    if (value.startsWith("SRID=")) {
      final int index = value.indexOf(';', 5);
      if (index == -1) {
        throw new IllegalArgumentException("Error parsing Geometry - SRID not delimited with ';' ");
      } else {
        srid = Integer.parseInt(value.substring(5, index));
        wkt = value.substring(index + 1).trim();
      }
    } else {
      wkt = value;
    }
    if (wkt.startsWith("00") || wkt.startsWith("01")) {
      return parse(wkt);
    } else {
      final GeometryFactory geometryFactory = GeometryFactory.floating(srid, 3);
      return geometryFactory.geometry(value);
    }
  }

  private Geometry parse(final String value) {
    final InputStream in = new StringByteInputStream(value);
    final ValueGetter valueGetter = ValueGetter.create(in);
    return parseGeometry(valueGetter);
  }

  private GeometryCollection parseCollection(final GeometryFactory geometryFactory,
    final ValueGetter data) {
    final int count = data.getInt();
    final Geometry[] geoms = new Geometry[count];
    parseGeometryArray(data, geoms);
    return geometryFactory.geometryCollection(geoms);
  }

  private double[] parseCoordinates(final int axisCount, final ValueGetter data, final boolean hasZ,
    final boolean hasM) {
    final int vertexCount = data.getInt();
    final double[] coordinates = new double[axisCount * vertexCount];

    int coordinateIndex = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
      final double x = data.getDouble();
      coordinates[coordinateIndex++] = x;

      final double y = data.getDouble();
      coordinates[coordinateIndex++] = y;

      if (hasM) {
        if (hasZ) {
          final double z = data.getDouble();
          coordinates[coordinateIndex++] = z;

          final double m = data.getDouble();
          coordinates[coordinateIndex++] = m;
        } else {
          coordinateIndex++; // no z so increment index
          final double m = data.getDouble();
          coordinates[coordinateIndex++] = m;
        }
      } else if (hasZ) {
        final double z = data.getDouble();
        coordinates[coordinateIndex++] = z;
      }
    }
    return coordinates;
  }

  private Geometry parseGeometry(final ValueGetter data) {
    final int typeword = data.getInt();

    final int realtype = typeword & 0x1FFFFFFF;

    final boolean hasZ = (typeword & 0x80000000) != 0;
    final boolean hasM = (typeword & 0x40000000) != 0;
    final boolean hasS = (typeword & 0x20000000) != 0;

    int srid = 0;
    if (hasS) {
      srid = data.getInt();
    }
    GeometryFactory geometryFactory;
    if (hasM) {
      geometryFactory = GeometryFactory.floating(srid, 4);
    } else if (hasZ) {
      geometryFactory = GeometryFactory.floating(srid, 3);
    } else {
      geometryFactory = GeometryFactory.floating(srid, 2);
    }

    switch (realtype) {
      case 1:
        return parsePoint(geometryFactory, data, hasZ, hasM);
      case 2:
        return parseLineString(geometryFactory, data, hasZ, hasM);
      case 3:
        return parsePolygon(geometryFactory, data, hasZ, hasM);
      case 4:
        return parseMultiPoint(geometryFactory, data);
      case 5:
        return parseMultiLineString(geometryFactory, data);
      case 6:
        return parseMultiPolygon(geometryFactory, data);
      case 7:
        return parseCollection(geometryFactory, data);
      default:
        throw new IllegalArgumentException("Unknown Geometry Type: " + realtype);
    }

  }

  private void parseGeometryArray(final ValueGetter data, final Geometry[] container) {
    for (int i = 0; i < container.length; ++i) {
      data.getByte(); // read endian
      container[i] = parseGeometry(data);
    }
  }

  private LinearRing parseLinearRing(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  private LineString parseLineString(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private Geometry parseMultiLineString(final GeometryFactory geometryFactory,
    final ValueGetter data) {
    final int count = data.getInt();
    final LineString[] lines = new LineString[count];
    parseGeometryArray(data, lines);
    if (lines.length == 1) {
      return lines[0];
    } else {
      return geometryFactory.multiLineString(lines);
    }
  }

  private Geometry parseMultiPoint(final GeometryFactory geometryFactory, final ValueGetter data) {
    final Point[] points = new Point[data.getInt()];
    parseGeometryArray(data, points);
    if (points.length == 1) {
      return points[0];
    } else {
      return geometryFactory.multiPoint(points);
    }
  }

  private Geometry parseMultiPolygon(final GeometryFactory geometryFactory,
    final ValueGetter data) {
    final int count = data.getInt();
    final Polygon[] polys = new Polygon[count];
    parseGeometryArray(data, polys);
    if (polys.length == 1) {
      return polys[0];
    } else {
      return geometryFactory.multiPolygon(polys);
    }
  }

  private Point parsePoint(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final double x = data.getDouble();
    final double y = data.getDouble();

    if (hasM) {
      if (hasZ) {
        final double z = data.getDouble();
        final double m = data.getDouble();
        return geometryFactory.point(x, y, z, m);
      } else {
        final double m = data.getDouble();
        return geometryFactory.point(x, y, Double.NaN, m);
      }
    } else if (hasZ) {
      final double z = data.getDouble();
      return geometryFactory.point(x, y, z);
    } else {
      return geometryFactory.point(x, y);
    }
  }

  private Polygon parsePolygon(final GeometryFactory geometryFactory, final ValueGetter data,
    final boolean hasZ, final boolean hasM) {
    final int count = data.getInt();
    final LinearRing[] rings = new LinearRing[count];
    for (int i = 0; i < count; ++i) {
      rings[i] = parseLinearRing(geometryFactory, data, hasZ, hasM);
    }
    return geometryFactory.polygon(rings);
  }

  public void setGeometry(final Geometry geometry) {
    this.value = PostgreSQLWktWriter.toString(geometry);
    ;
  }
}
