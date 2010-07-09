package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.python.modules.synchronize;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class OracleSdoGeometryJdbcAttribute extends JdbcAttribute {
  public static final String COORDINATE_DIMENSION_PROPERTY = "coordinateDimension";

  public static final String COORDINATE_PRECISION_PROPERTY = "coordinatePrecision";

  public static final QName SCHEMA_PROPERTY = new QName(
    OracleSdoGeometryJdbcAttribute.class.getName());

  public static final String SRID_PROPERTY = "srid";

  private final CoordinateSystem coordinateSystem;

  private final int dimension;

  private final GeometryFactory geometryFactory;

  private final PrecisionModel[] precisionModels;

  private QName typeName;

  public OracleSdoGeometryJdbcAttribute(
    final String name,
    final DataType type,
    final int sqlType,
    final int length,
    final int scale,
    final boolean required,
    final Map<QName, Object> properties,
    final GeometryFactory geometryFactory,
    final int dimension) {
    super(name, type, sqlType, length, scale, required, properties);
    this.geometryFactory = geometryFactory;
    this.coordinateSystem = geometryFactory.getCoordinateSystem();
    this.dimension = dimension;
    this.precisionModels = new PrecisionModel[dimension];
    final PrecisionModel precisionModel = geometryFactory.getPrecisionModel();
    if (precisionModel != null) {
      precisionModels[0] = precisionModel;
      precisionModels[1] = precisionModel;
    }
    for (int i = 0; i < precisionModels.length; i++) {
      if (precisionModels[i] == null) {
        precisionModels[i] = new PrecisionModel();
      }
    }
  }

  @Override
  public OracleSdoGeometryJdbcAttribute clone() {
    return new OracleSdoGeometryJdbcAttribute(getName(), getType(),
      getSqlType(), getLength(), getScale(), isRequired(), getProperties(),
      geometryFactory, dimension);
  }

  @Override
  public void addColumnName(
    StringBuffer sql,
    String tablePrefix) {
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
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    Geometry value;
    int geometryType = resultSet.getInt(columnIndex);
    final int numAxis = geometryType / 1000;
    switch (geometryType % 1000) {
      case 1:
        value = toPoint(resultSet, columnIndex, numAxis);
      break;
      case 2:
        value = toLineString(resultSet, columnIndex, numAxis);
      break;
      case 3:
        value = toPolygon(resultSet, columnIndex, numAxis);
      break;
      default:
        throw new IllegalArgumentException("Unsupported geometry type "
          + geometryType);
    }
    object.setValue(getIndex(), value);
    return columnIndex + 6;
  }

  @Override
  public int setInsertPreparedStatementValue(
    PreparedStatement statement,
    int parameterIndex,
    DataObject object)
    throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      synchronized (STRUCT.class) {
        final Connection connection = statement.getConnection();
        final STRUCT oracleValue = toJdbc(connection, value, dimension);
        ((OraclePreparedStatement)statement).setSTRUCT(parameterIndex,
          oracleValue);
      }
    }
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value)
    throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      synchronized (STRUCT.class) {
        final Connection connection = statement.getConnection();
        final STRUCT oracleValue = toJdbc(connection, value, 2);
        ((OraclePreparedStatement)statement).setSTRUCT(parameterIndex,
          oracleValue);
      }
    }
    return parameterIndex + 1;
  }

  // public synchronized Object toJava(
  // final Object object)
  // throws SQLException {
  // if (object instanceof STRUCT) {
  // final STRUCT struct = (STRUCT)object;
  // final Datum[] attributes = struct.getOracleAttributes();
  // final int geometryType = attributes[0].intValue();
  // final int numAxis = geometryType / 1000;
  // switch (geometryType % 1000) {
  // case 1:
  // return toPoint(attributes, numAxis);
  // case 2:
  // return toLineString(attributes, numAxis);
  // case 3:
  // return toPolygon(attributes, numAxis);
  // default:
  // throw new IllegalArgumentException("Unsupported geometry type "
  // + geometryType);
  // }
  // }
  // return object;
  //
  // }

  private STRUCT toJdbc(
    final Connection connection,
    final Object object,
    int dimension)
    throws SQLException {
    if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = GeometryProjectionUtil.perform(geometry, coordinateSystem);
      // TODO direct convert to SDO Geometry from JTS Geometry
      JGeometry jGeometry = null;
      if (object instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        jGeometry = toJGeometry(polygon, dimension);
      } else if (object instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        jGeometry = toJGeometry(lineString, dimension);
      } else if (object instanceof Point) {
        final Point point = (Point)geometry;
        jGeometry = toJGeometry(point, dimension);
      } else {
        throw new IllegalArgumentException("Unable to convert to SDO_GEOMETRY "
          + object.getClass());
      }
      try {
        final STRUCT struct = JGeometry.store(jGeometry, connection);
        return struct;
      } catch (final SQLException e) {
        throw new RuntimeException(
          "Unable to convert Oracle JGeometry to STRUCT: " + e.getMessage(), e);
      }
    } else {
      throw new IllegalArgumentException("Unable to convert to SDO_GEOMETRY "
        + object.getClass());
    }
  }

  private JGeometry toJGeometry(
    final LineString lineString,
    int dimension) {
    final double[] ordinates = toOrdinateArray(lineString, dimension);
    return JGeometry.createLinearLineString(ordinates, dimension,
      geometryFactory.getSRID());
  }

  private JGeometry toJGeometry(
    final Point point,
    int dimension) {
    final Coordinate coordinate = point.getCoordinate();
    final int srid = geometryFactory.getSRID();
    final double x = coordinate.x;
    final double y = coordinate.y;
    if (dimension == 3) {
      double z = coordinate.z;
      if (Double.isNaN(z)) {
        z = 0;
      }
      return new JGeometry(x, y, z, srid);
    } else {
      return new JGeometry(x, y, srid);
    }
  }

  private JGeometry toJGeometry(
    final Polygon polygon,
    int dimension) {
    final Object[] oridinateArrays = toOrdinateArrays(polygon, dimension);
    return JGeometry.createLinearPolygon(oridinateArrays, dimension,
      geometryFactory.getSRID());
  }

  private LineString toLineString(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);
    final double[] coordinates = coordinatesArray.getDoubleArray();
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis,
      coordinates);
    return geometryFactory.createLineString(coordinatesList);
  }

  private double[] toOrdinateArray(
    final CoordinateSequence sequence,
    int dimension) {
    int geometryDimension = sequence.getDimension();
    if (Double.isNaN(sequence.getOrdinate(0, 2))) {
      geometryDimension = 2;
    }

    final double[] ordinates = new double[dimension * sequence.size()];
    int ordinateIndex = 0;

    for (int i = 0; i < sequence.size(); i++) {
      for (int j = 0; j < dimension; j++) {
        if (j >= geometryDimension) {
          ordinates[ordinateIndex++] = 0;
        } else {
          final double ordinate = sequence.getOrdinate(i, j);
          if (Double.isNaN(ordinate)) {
            ordinates[ordinateIndex++] = 0;
          } else {
            final PrecisionModel precisionModel = precisionModels[j];
            ordinates[ordinateIndex++] = precisionModel.makePrecise(ordinate);
          }
        }
      }
    }
    return ordinates;
  }

  private double[] toOrdinateArray(
    final LineString line,
    int dimension) {
    final CoordinateSequence sequence = line.getCoordinateSequence();
    final double[] ordinates = toOrdinateArray(sequence, dimension);
    return ordinates;
  }

  private Object[] toOrdinateArrays(
    final Polygon polygon,
    int dimension) {
    final int numInteriorRing = polygon.getNumInteriorRing();
    final Object[] ordinateArrays = new Object[1 + numInteriorRing];

    final LineString exteriorRing = polygon.getExteriorRing();
    ordinateArrays[0] = toOrdinateArray(exteriorRing, dimension);

    for (int i = 0; i < numInteriorRing; i++) {
      final LineString ring = polygon.getInteriorRingN(i);
      ordinateArrays[i + 1] = toOrdinateArray(ring, dimension);

    }
    return ordinateArrays;
  }

  private Point toPoint(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    final CoordinatesList coordinatesList;
    double x = resultSet.getDouble(columnIndex + 1);
    double y = resultSet.getDouble(columnIndex + 2);
    if (numAxis == 2) {
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y);
    } else {
      double z = resultSet.getDouble(columnIndex + 3);
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y, z);
    }
    return geometryFactory.createPoint(coordinatesList);
  }

  private Polygon toPolygon(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    final ARRAY elemInfoArray = (ARRAY)resultSet.getArray(columnIndex + 4);
    final long[] elemInfo = elemInfoArray.getLongArray();
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    LinearRing exteriorRing = null;
    final LinearRing[] interiorRings = new LinearRing[elemInfo.length / 3 - 1];
    int numInteriorRings = 0;

    for (int i = 0; i < elemInfo.length; i += 3) {
      final long offset = elemInfo[i];
      final long type = elemInfo[i + 1];
      final long interpretation = elemInfo[i + 2];
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3];
        length = (int)(nextOffset - offset) + 1;
      } else {
        length = (int)(coordinatesArray.length() - offset) + 1;
      }
      if (interpretation == 1) {
        final double[] ordinates = coordinatesArray.getDoubleArray(offset,
          length);
        final CoordinatesList coordinatesList = new DoubleCoordinatesList(
          numAxis, ordinates);
        final LinearRing ring = geometryFactory.createLinearRing(coordinatesList);

        switch ((int)type) {
          case 1003:
            if (exteriorRing == null) {
              exteriorRing = ring;
            } else {
              throw new IllegalArgumentException(
                "Cannot have two exterior rings on a geometry");
            }
          break;
          case 2003:
            if (numInteriorRings == interiorRings.length) {
              throw new IllegalArgumentException("Too many interior rings");
            } else {
              interiorRings[numInteriorRings++] = ring;
            }
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type "
              + type);
        }
      } else {
        throw new IllegalArgumentException("Unsupported geometry type " + type
          + " interpretation " + interpretation);
      }
    }
    final Polygon polygon = geometryFactory.createPolygon(exteriorRing,
      interiorRings);
    return polygon;
  }
}
