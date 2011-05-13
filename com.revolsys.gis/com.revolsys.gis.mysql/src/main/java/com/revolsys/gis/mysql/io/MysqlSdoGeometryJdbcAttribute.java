package com.revolsys.gis.mysql.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class MysqlSdoGeometryJdbcAttribute extends JdbcAttribute {
  public static final String COORDINATE_DIMENSION_PROPERTY = "coordinateDimension";

  public static final String COORDINATE_PRECISION_PROPERTY = "coordinatePrecision";

  public static final QName SCHEMA_PROPERTY = new QName(
    MysqlSdoGeometryJdbcAttribute.class.getName());

  public static final String SRID_PROPERTY = "srid";

  private final CoordinateSystem coordinateSystem;

  private final int dimension;

  private final GeometryFactory geometryFactory;

  private final PrecisionModel[] precisionModels;

  private QName typeName;

  public MysqlSdoGeometryJdbcAttribute(
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
    setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @Override
  public void addColumnName(
    final StringBuffer sql,
    final String tablePrefix) {
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
  public MysqlSdoGeometryJdbcAttribute clone() {
    return new MysqlSdoGeometryJdbcAttribute(getName(), getType(),
      getSqlType(), getLength(), getScale(), isRequired(), getProperties(),
      geometryFactory, dimension);
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    Geometry value;
    final int geometryType = resultSet.getInt(columnIndex);
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
      case 7:
        value = toMultiPolygon(resultSet, columnIndex, numAxis);
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
    final PreparedStatement statement,
    final int parameterIndex,
    final DataObject object)
    throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
   
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

  public double[] toClockwiseCoordinatesArray(
    final LineString ring,
    final int dimension) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(ring);
    if (!CoordinatesListUtil.isCCW(coordinates)) {
      return toCoordinateArray(coordinates, dimension);
    } else {
      return toCoordinateArray(coordinates.reverse(), dimension);
    }
  }

  private double[] toCoordinateArray(
    final CoordinateSequence sequence,
    final int dimension) {
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

  private double[] toCoordinateArray(
    final LineString line,
    final int dimension) {
    final CoordinateSequence sequence = line.getCoordinateSequence();
    final double[] coordinates = toCoordinateArray(sequence, dimension);
    return coordinates;
  }

  private double[][] toCoordinateArrays(
    final Polygon polygon,
    final int dimension) {
    final int numInteriorRing = polygon.getNumInteriorRing();
    final double[][] ordinateArrays = new double[1 + numInteriorRing][];

    final LineString exteriorRing = polygon.getExteriorRing();
    ordinateArrays[0] = toCounterClockwiseCoordinatesArray(exteriorRing,
      dimension);

    for (int i = 0; i < numInteriorRing; i++) {
      final LineString ring = polygon.getInteriorRingN(i);
      ordinateArrays[i + 1] = toClockwiseCoordinatesArray(ring, dimension);
    }
    return ordinateArrays;
  }

  public double[] toCounterClockwiseCoordinatesArray(
    final LineString ring,
    final int dimension) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(ring);
    if (CoordinatesListUtil.isCCW(coordinates)) {
      return toCoordinateArray(coordinates, dimension);
    } else {
      return toCoordinateArray(coordinates.reverse(), dimension);
    }
  }

  private LineString toLineString(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    final double[] coordinates = null;
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis,
      coordinates);
    return geometryFactory.createLineString(coordinatesList);
  }

  private Point toPoint(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    final CoordinatesList coordinatesList;
    final double x = resultSet.getDouble(columnIndex + 1);
    final double y = resultSet.getDouble(columnIndex + 2);
    if (numAxis == 2) {
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y);
    } else {
      final double z = resultSet.getDouble(columnIndex + 3);
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y, z);
    }
    return geometryFactory.createPoint(coordinatesList);
  }

  private Polygon toPolygon(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
   
    LinearRing exteriorRing = null;
    final LinearRing[] interiorRings = new LinearRing[0];
  
    final Polygon polygon = geometryFactory.createPolygon(exteriorRing,
      interiorRings);
    return polygon;
  }

  private MultiPolygon toMultiPolygon(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    List<Polygon> polygons = new ArrayList<Polygon>();

    LinearRing exteriorRing = null;
    List<LinearRing> interiorRings = null;
    int numInteriorRings = 0;

    if (exteriorRing != null) {
      final Polygon polygon = geometryFactory.createPolygon(exteriorRing,
        GeometryFactory.toLinearRingArray(interiorRings));
      polygons.add(polygon);
    }

    return geometryFactory.createMultiPolygon(polygons);
  }
}
