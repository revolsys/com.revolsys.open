package com.revolsys.geopackage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.property.FieldProperties;

public class GeoPackageGeometryJdbcFieldDefinition extends JdbcFieldDefinition {
  private final int axisCount;

  private final GeometryFactory geometryFactory;

  private final int srid;

  public GeoPackageGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType dataType, final boolean required, final String description,
    final Map<String, Object> properties, final int srid, final int axisCount,
    final GeometryFactory geometryFactory) {
    super(dbName, name, dataType, -1, 0, 0, required, description, properties);
    this.srid = srid;
    this.geometryFactory = geometryFactory;
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
    this.axisCount = axisCount;
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new GeoPackageGeometryJdbcFieldDefinition(getDbName(), getName(), getDataType(),
      isRequired(), getDescription(), getProperties(), this.srid, this.axisCount,
      this.geometryFactory);
  }

  public Object getInsertUpdateValue(final Object object) throws SQLException {
    if (object == null) {
      return null;
    } else if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      // TODO
      return null;
    } else if (object instanceof BoundingBox) {
      // TODO
      return null;
    } else {
      return object;
    }
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final Object databaseValue = resultSet.getObject(columnIndex);
    return toJava(databaseValue);
  }

  private Geometry parseCollection(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final int count = data.getInt();
    final Geometry[] geoms = new Geometry[count];
    parseGeometryArray(geometryFactory, data, geoms);
    return geometryFactory.geometry(geoms);
  }

  private double[] parseCoordinates(final int axisCount, final ByteBuffer data, final boolean hasZ,
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

  private Geometry parseGeometry(GeometryFactory geometryFactory, final ByteBuffer buffer) {
    ByteOrder byteOrder;
    if (buffer.get() == 0) {
      byteOrder = ByteOrder.BIG_ENDIAN;
    } else {
      byteOrder = ByteOrder.LITTLE_ENDIAN;
    }
    buffer.order(byteOrder);

    final int flags = buffer.getInt();

    final int geometryType = flags % 1000;
    final int geometryTypeMode = flags / 1000;

    boolean hasZ = false;
    boolean hasM = false;
    switch (geometryTypeMode) {
      case 0:
      break;

      case 1:
        hasZ = true;
      break;

      case 2:
        hasM = true;
      break;

      case 3:
        hasZ = true;
        hasM = true;
      break;
    }
    int axisCount;
    if (hasM) {
      axisCount = 4;
    } else if (hasZ) {
      axisCount = 3;
    } else {
      axisCount = 2;
    }
    if (axisCount != geometryFactory.getAxisCount()) {
      geometryFactory = geometryFactory.convertAxisCount(axisCount);
    }
    Geometry geometry;
    switch (geometryType) {
      case 1:
        geometry = parsePoint(geometryFactory, buffer, hasZ, hasM);
      break;
      case 2:
        geometry = parseLineString(geometryFactory, buffer, hasZ, hasM);
      break;
      case 3:
        geometry = parsePolygon(geometryFactory, buffer, hasZ, hasM);
      break;
      case 4:
        geometry = parseMultiPoint(geometryFactory, buffer);
      break;
      case 5:
        geometry = parseMultiLineString(geometryFactory, buffer);
      break;
      case 6:
        geometry = parseMultiPolygon(geometryFactory, buffer);
      break;
      case 7:
        geometry = parseCollection(geometryFactory, buffer);
      break;
      default:
        throw new IllegalArgumentException("Unknown Geometry Type: " + geometryType);
    }
    return geometry;
  }

  private void parseGeometryArray(final GeometryFactory geometryFactory, final ByteBuffer data,
    final Geometry[] container) {
    for (int i = 0; i < container.length; ++i) {
      data.get(); // read endian
      container[i] = parseGeometry(geometryFactory, data);
    }
  }

  private LinearRing parseLinearRing(final GeometryFactory geometryFactory, final ByteBuffer data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  private LineString parseLineString(final GeometryFactory geometryFactory, final ByteBuffer data,
    final boolean hasZ, final boolean hasM) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = parseCoordinates(axisCount, data, hasZ, hasM);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  private Geometry parseMultiLineString(final GeometryFactory geometryFactory,
    final ByteBuffer data) {
    final int count = data.getInt();
    final LineString[] lines = new LineString[count];
    parseGeometryArray(geometryFactory, data, lines);
    if (lines.length == 1) {
      return lines[0];
    } else {
      return geometryFactory.lineal(lines);
    }
  }

  private Geometry parseMultiPoint(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final Point[] points = new Point[data.getInt()];
    parseGeometryArray(geometryFactory, data, points);
    if (points.length == 1) {
      return points[0];
    } else {
      return geometryFactory.punctual(points);
    }
  }

  private Geometry parseMultiPolygon(final GeometryFactory geometryFactory, final ByteBuffer data) {
    final int count = data.getInt();
    final Polygon[] polys = new Polygon[count];
    parseGeometryArray(geometryFactory, data, polys);
    if (polys.length == 1) {
      return polys[0];
    } else {
      return geometryFactory.polygonal(polys);
    }
  }

  private Point parsePoint(final GeometryFactory geometryFactory, final ByteBuffer data,
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

  private Polygon parsePolygon(final GeometryFactory geometryFactory, final ByteBuffer data,
    final boolean hasZ, final boolean hasM) {
    final int count = data.getInt();
    final LinearRing[] rings = new LinearRing[count];
    for (int i = 0; i < count; ++i) {
      rings[i] = parseLinearRing(geometryFactory, data, hasZ, hasM);
    }
    return geometryFactory.polygon(rings);
  }

  private Geometry parseWkb(GeometryFactory geometryFactory, final byte[] data) {
    final ByteBuffer buffer = ByteBuffer.wrap(data);
    if (buffer.get() == 'G') {
      if (buffer.get() == 'P') {
        final byte version = buffer.get();
        final byte flags = buffer.get();

        final boolean extended = (flags >> 5 & 1) == 1;

        final boolean empty = (flags >> 4 & 1) == 1;

        final int envelopeType = flags >> 1 & 7;

        ByteOrder byteOrder;
        if ((flags & 1) == 0) {
          byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
          byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
        buffer.order(byteOrder);
        final int coordinateSystemId = buffer.getInt();
        geometryFactory = geometryFactory.convertSrid(coordinateSystemId);
        int envelopeCoordinateCount = 0;
        switch (envelopeType) {
          case 1:
            envelopeCoordinateCount = 4;
          break;
          case 2:
            envelopeCoordinateCount = 6;
          break;
          case 3:
            envelopeCoordinateCount = 6;
          break;
          case 4:
            envelopeCoordinateCount = 8;
          break;

          default:
          break;
        }
        for (int i = 0; i < envelopeCoordinateCount; i++) {
          buffer.getDouble();
        }
        return parseGeometry(geometryFactory, buffer);
      }
    }
    throw new IllegalArgumentException(
      "Invalid Geometry header, expecting GP\n" + Arrays.toString(data));
  }


  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    final Object jdbcValue = getInsertUpdateValue(value);
    if (jdbcValue == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      statement.setObject(parameterIndex, jdbcValue);
    }
    return parameterIndex + 1;
  }
  

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    final Object jdbcValue = toJdbc(value);
    if (jdbcValue == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      statement.setObject(parameterIndex, jdbcValue);
    }
    return parameterIndex + 1;
  }

  public Object toJava(final Object object) throws SQLException {
    if (object instanceof byte[]) {
      final byte[] bytes = (byte[])object;
      return parseWkb(this.geometryFactory, bytes);
    }
    return object;
  }

  public Object toJdbc(final Object object) throws SQLException {
    if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      if (geometry.isEmpty()) {
        return null;
      } else {
        // TODO
        return null;
      }
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.bboxToCs(this.geometryFactory);
      // TODO
      return null;
    } else {
      return object;
    }
  }
}
