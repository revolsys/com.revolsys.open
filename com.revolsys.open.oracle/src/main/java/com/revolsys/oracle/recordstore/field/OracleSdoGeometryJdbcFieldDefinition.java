package com.revolsys.oracle.recordstore.field;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.util.number.Numbers;

public class OracleSdoGeometryJdbcFieldDefinition extends JdbcFieldDefinition {

  private static final int[] LINESTRING_ELEM_INFO = new int[] {
    1, 2, 1
  };

  private static final String MDSYS_SDO_GEOMETRY = "MDSYS.SDO_GEOMETRY";

  private static final String MDSYS_SDO_POINT_TYPE = "MDSYS.SDO_POINT_TYPE";

  private static final int[] RECTANGLE_ELEM_INFO = new int[] {
    1, 1003, 3
  };

  private static final double NAN_VALUE = 0;

  private final int axisCount;

  private final GeometryFactory geometryFactory;

  private final int oracleSrid;

  public OracleSdoGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType type, final int sqlType, final boolean required, final String description,
    final Map<String, Object> properties, final GeometryFactory geometryFactory,
    final int axisCount, final int oracleSrid) {
    super(dbName, name, type, sqlType, 0, 0, required, description, properties);
    this.geometryFactory = geometryFactory;
    this.axisCount = axisCount;
    this.oracleSrid = oracleSrid;
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @Override
  public void addColumnName(final StringBuilder sql, final String tablePrefix) {
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_GTYPE, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.X, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.Y, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.Z, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_ELEM_INFO, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_ORDINATES");
  }

  @Override
  public OracleSdoGeometryJdbcFieldDefinition clone() {
    return new OracleSdoGeometryJdbcFieldDefinition(getDbName(), getName(), getDataType(),
      getSqlType(), isRequired(), getDescription(), getProperties(), this.geometryFactory,
      this.axisCount, this.oracleSrid);
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    Geometry value;
    final int geometryType = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      final int axisCount = geometryType / 1000;
      switch (geometryType % 1000) {
        case 1:
          value = toPoint(resultSet, columnIndex, axisCount);
        break;
        case 2:
          value = toLineString(resultSet, columnIndex, axisCount);
        break;
        case 3:
          value = toPolygon(resultSet, columnIndex, axisCount);
        break;
        case 5:
          value = toMultiPoint(resultSet, columnIndex, axisCount);
        break;
        case 6:
          value = toMultiLineString(resultSet, columnIndex, axisCount);
        break;
        case 7:
          value = toMultiPolygon(resultSet, columnIndex, axisCount);
        break;
        default:
          throw new IllegalArgumentException("Unsupported geometry type " + geometryType);
      }
      object.setValue(getIndex(), value);
    }
    return columnIndex + 6;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final Connection connection = statement.getConnection();
      final Struct oracleValue = toSdoGeometry(connection, value, this.axisCount);
      statement.setObject(parameterIndex, oracleValue);
    }
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final Connection connection = statement.getConnection();
      final Struct oracleValue = toSdoGeometry(connection, value, 2);
      statement.setObject(parameterIndex, oracleValue);
    }
    return parameterIndex + 1;
  }

  private LineString toLineString(final ResultSet resultSet, final int columnIndex,
    final int axisCount) throws SQLException {
    final int index = columnIndex + 5;
    final BigDecimal[] coordinates = JdbcUtils.getBigDecimalArray(resultSet, index);
    return this.geometryFactory.lineString(axisCount, coordinates);
  }

  private MultiLineString toMultiLineString(final ResultSet resultSet, final int columnIndex,
    final int axisCount) throws SQLException {
    final List<LineString> lines = new ArrayList<>();

    final BigDecimal[] elemInfo = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 4);
    final BigDecimal[] coordinatesArray = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 5);

    for (int i = 0; i < elemInfo.length; i += 3) {
      final int offset = elemInfo[i].intValue();
      final int type = elemInfo[i + 1].intValue();
      final int interpretation = elemInfo[i + 2].intValue();
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3].intValue();
        length = (int)(nextOffset - offset);
      } else {
        length = coordinatesArray.length - offset + 1;
      }
      if (interpretation == 1) {
        final double[] coordinates = Numbers.toDoubleArray(coordinatesArray, offset - 1, length);
        final LineString points = this.geometryFactory.lineString(axisCount, coordinates);
        lines.add(points);
      } else {
        throw new IllegalArgumentException(
          "Unsupported geometry type " + type + " interpretation " + interpretation);
      }
    }

    return this.geometryFactory.multiLineString(lines);
  }

  private MultiPoint toMultiPoint(final ResultSet resultSet, final int columnIndex,
    final int axisCount) throws SQLException {
    final BigDecimal[] coordinatesArray = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 5);

    final double[] coordinates = Numbers.toDoubleArray(coordinatesArray);
    final LineString coordinatesList = new LineStringDouble(axisCount, coordinates);

    return this.geometryFactory.multiPoint(coordinatesList);
  }

  private MultiPolygon toMultiPolygon(final ResultSet resultSet, final int columnIndex,
    final int axisCount) throws SQLException {
    final List<Polygon> polygons = new ArrayList<Polygon>();

    final BigDecimal[] elemInfo = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 4);
    final BigDecimal[] coordinatesArray = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 5);
    final int coordinateCount = coordinatesArray.length;

    List<LinearRing> rings = Collections.emptyList();

    for (int i = 0; i < elemInfo.length; i += 3) {
      final int offset = elemInfo[i].intValue();
      final long type = elemInfo[i + 1].longValue();
      final long interpretation = elemInfo[i + 2].longValue();
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3].longValue();
        length = (int)(nextOffset - offset);
      } else {
        length = coordinateCount + 1 - offset;
      }
      if (interpretation == 1) {
        final double[] coordinates = Numbers.toDoubleArray(coordinatesArray, offset - 1, length);
        final LinearRing ring = this.geometryFactory.linearRing(axisCount, coordinates);

        switch ((int)type) {
          case 1003:
            if (!rings.isEmpty()) {
              final Polygon polygon = this.geometryFactory.polygon(rings);
              polygons.add(polygon);
            }
            rings = new ArrayList<>();
            rings.add(ring);

          break;
          case 2003:
            rings.add(ring);
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type " + type);
        }
      } else {
        throw new IllegalArgumentException(
          "Unsupported geometry type " + type + " interpretation " + interpretation);
      }
    }
    if (!rings.isEmpty()) {
      final Polygon polygon = this.geometryFactory.polygon(rings);
      polygons.add(polygon);
    }

    return this.geometryFactory.multiPolygon(polygons);
  }

  private Point toPoint(final ResultSet resultSet, final int columnIndex, final int axisCount)
    throws SQLException {
    final double x = resultSet.getDouble(columnIndex + 1);
    final double y = resultSet.getDouble(columnIndex + 2);
    if (axisCount == 2) {
      return this.geometryFactory.point(x, y);
    } else {
      final double z = resultSet.getDouble(columnIndex + 3);
      return this.geometryFactory.point(x, y, z);
    }
  }

  private Polygon toPolygon(final ResultSet resultSet, final int columnIndex, final int axisCount)
    throws SQLException {
    final BigDecimal[] elemInfo = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 4);
    final BigDecimal[] coordinatesArray = JdbcUtils.getBigDecimalArray(resultSet, columnIndex + 5);

    final List<LinearRing> rings = new ArrayList<>();
    int numInteriorRings = 0;

    for (int i = 0; i < elemInfo.length; i += 3) {
      final int offset = elemInfo[i].intValue();
      final long type = elemInfo[i + 1].longValue();
      final long interpretation = elemInfo[i + 2].longValue();
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3].longValue();
        length = (int)(nextOffset - offset);
      } else {
        length = coordinatesArray.length - offset + 1;
      }
      if (interpretation == 1) {
        final double[] coordinates = Numbers.toDoubleArray(coordinatesArray, offset - 1, length);
        final LinearRing ring = this.geometryFactory.linearRing(axisCount, coordinates);

        switch ((int)type) {
          case 1003:
            if (rings.isEmpty()) {
              rings.add(ring);
            } else {
              throw new IllegalArgumentException("Cannot have two exterior rings on a geometry");
            }
          break;
          case 2003:
            if (numInteriorRings == rings.size()) {
              throw new IllegalArgumentException("Too many interior rings");
            } else {
              numInteriorRings++;
              rings.add(ring);
            }
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type " + type);
        }
      } else {
        throw new IllegalArgumentException(
          "Unsupported geometry type " + type + " interpretation " + interpretation);
      }
    }
    final Polygon polygon = this.geometryFactory.polygon(rings);
    return polygon;
  }

  private Struct toSdoGeometry(final Connection connection, final int geometryType,
    final Struct pointStruct, final int[] elemInfo, final double... coordinates)
    throws SQLException {
    return JdbcUtils.struct(connection, MDSYS_SDO_GEOMETRY, geometryType, this.oracleSrid,
      pointStruct, elemInfo, coordinates);
  }

  private Struct toSdoGeometry(final Connection connection, final Object object,
    final int axisCount) throws SQLException {
    if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = geometry.copy(this.geometryFactory);
      if (object instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return toSdoPolygon(connection, polygon, axisCount);
      } else if (object instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return toSdoLineString(connection, lineString, axisCount);
      } else if (object instanceof Point) {
        final Point point = (Point)geometry;
        return toSdoPoint(connection, point, axisCount);
      } else if (object instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        return toSdoMultiPoint(connection, multiPoint, axisCount);
      } else if (object instanceof MultiLineString) {
        final MultiLineString multiLineString = (MultiLineString)geometry;
        return toSdoMultiLineString(connection, multiLineString, axisCount);
      } else if (object instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        return toSdoMultiPolygon(connection, multiPolygon, axisCount);
      }
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.convert(this.geometryFactory, 2);
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      return toSdoGeometry(connection, 3, null, RECTANGLE_ELEM_INFO, minX, minY, maxX, maxY);
    }
    throw new IllegalArgumentException("Unable to convert to SDO_GEOMETRY " + object.getClass());
  }

  private Struct toSdoLineString(final Connection connection, final LineString line,
    final int axisCount) throws SQLException {
    final int geometryType = axisCount * 1000 + 2;
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    line.copyCoordinates(axisCount, NAN_VALUE, coordinates, 0);
    return toSdoGeometry(connection, geometryType, null, LINESTRING_ELEM_INFO, coordinates);
  }

  private Struct toSdoMultiLineString(final Connection connection,
    final MultiLineString multiLineString, final int axisCount) throws SQLException {
    final int geometryType = axisCount * 1000 + 6;

    final int geometryCount = multiLineString.getGeometryCount();
    final int[] elemInfo = new int[geometryCount * 3];

    final int vertexCount = multiLineString.getVertexCount();
    final int coordinateCount = vertexCount * axisCount;
    final double[] coordinates = new double[coordinateCount];
    int offset = 0;
    int elemIndex = 0;
    for (final LineString line : multiLineString.lineStrings()) {
      elemInfo[elemIndex++] = offset + 1;
      elemInfo[elemIndex++] = 2;
      elemInfo[elemIndex++] = 1;
      offset = line.copyCoordinates(axisCount, NAN_VALUE, coordinates, offset);
    }
    return toSdoGeometry(connection, geometryType, null, elemInfo, coordinates);
  }

  private Struct toSdoMultiPoint(final Connection connection, final MultiPoint multiPoint,
    final int axisCount) throws SQLException {
    final int geometryType = axisCount * 1000 + 5;

    final int geometryCount = multiPoint.getGeometryCount();
    final int[] elemInfo = new int[] {
      1, 1, geometryCount
    };

    final double[] coordinates = new double[geometryCount * axisCount];
    int i = 0;
    for (int partIndex = 0; partIndex < geometryCount; partIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double value = multiPoint.getCoordinate(partIndex, axisIndex);
        if (Double.isNaN(value)) {
          coordinates[i] = NAN_VALUE;
        } else {
          coordinates[i] = value;
        }
        i++;
      }
    }
    return toSdoGeometry(connection, geometryType, null, elemInfo, coordinates);
  }

  private Struct toSdoMultiPolygon(final Connection connection, final MultiPolygon multiPolygon,
    final int axisCount) throws SQLException {
    final int vertexCount = multiPolygon.getVertexCount();
    final int coordinateCount = vertexCount * axisCount;
    final double[] coordinates = new double[coordinateCount];

    int ringCount = 0;
    for (final Polygon polygon : multiPolygon.polygons()) {
      ringCount += polygon.getRingCount();
    }
    final int[] elemInfo = new int[ringCount * 3];

    int offset = 0;
    int elemIndex = 0;
    for (final Polygon polygon : multiPolygon.polygons()) {
      int i = 0;
      for (final LineString line : polygon.rings()) {
        elemInfo[elemIndex++] = offset + 1;
        if (i == 0) {
          elemInfo[elemIndex++] = 1003; // Exterior counter clockwise
        } else {
          elemInfo[elemIndex++] = 2003; // Interior clockwise
        }
        elemInfo[elemIndex++] = 1;
        final boolean clockwise = line.isClockwise();
        if (clockwise == (i != 0)) {
          offset = line.copyCoordinates(axisCount, NAN_VALUE, coordinates, offset);
        } else {
          offset = line.copyCoordinatesReverse(axisCount, NAN_VALUE, coordinates, offset);
        }
        i++;
      }
    }
    final int geometryType = axisCount * 1000 + 7;
    return toSdoGeometry(connection, geometryType, null, elemInfo, coordinates);
  }

  private Struct toSdoPoint(final Connection connection, final Point point, int axisCount)
    throws SQLException {
    int geometryType = 1;
    if (axisCount == 3) {
      geometryType = 3001;
    } else if (axisCount > 3) {
      axisCount = 3;
      geometryType = 3001;
    }
    final double[] coordinates = new double[axisCount];
    point.copyCoordinates(axisCount, NAN_VALUE, coordinates, 0);
    final Struct pointStruct = JdbcUtils.struct(connection, MDSYS_SDO_POINT_TYPE, coordinates);
    return toSdoGeometry(connection, geometryType, pointStruct, null, null);
  }

  private Struct toSdoPolygon(final Connection connection, final Polygon polygon,
    final int axisCount) throws SQLException {
    final int geometryType = axisCount * 1000 + 3;

    final int ringCount = polygon.getRingCount();
    final int[] elemInfo = new int[ringCount * 3];

    final int vertexCount = polygon.getVertexCount();
    final int coordinateCount = vertexCount * axisCount;
    final double[] coordinates = new double[coordinateCount];
    int offset = 0;
    int elemIndex = 0;
    int i = 0;
    for (final LineString line : polygon.rings()) {
      elemInfo[elemIndex++] = offset + 1;
      if (i == 0) {
        elemInfo[elemIndex++] = 1003; // Exterior counter clockwise
      } else {
        elemInfo[elemIndex++] = 2003; // Interior clockwise
      }
      elemInfo[elemIndex++] = 1;
      final boolean clockwise = line.isClockwise();
      if (clockwise == (i != 0)) {
        offset = line.copyCoordinates(axisCount, NAN_VALUE, coordinates, offset);
      } else {
        offset = line.copyCoordinatesReverse(axisCount, NAN_VALUE, coordinates, offset);
      }
      i++;
    }
    return toSdoGeometry(connection, geometryType, null, elemInfo, coordinates);

  }
}
