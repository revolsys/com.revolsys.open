package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
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
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
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
  public OracleSdoGeometryJdbcAttribute clone() {
    return new OracleSdoGeometryJdbcAttribute(getName(), getType(),
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

  private STRUCT toJdbc(
    final Connection connection,
    final Object object,
    final int dimension)
    throws SQLException {
    if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = GeometryProjectionUtil.perform(geometry, geometryFactory);
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
      } else if (object instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        jGeometry = toJGeometry(multiPoint, dimension);
      } else if (object instanceof MultiLineString) {
        final MultiLineString multiLineString = (MultiLineString)geometry;
        jGeometry = toJGeometry(multiLineString, dimension);
      } else if (object instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        jGeometry = toJGeometry(multiPolygon, dimension);
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
    final int dimension) {
    final double[] ordinates = toCoordinateArray(lineString, dimension);
    final int srid = geometryFactory.getSRID();
    return JGeometry.createLinearLineString(ordinates, dimension, srid);
  }

  private JGeometry toJGeometry(
    final MultiLineString multiLineString,
    final int dimension) {
    return null;
  }

  private JGeometry toJGeometry(
    final MultiPoint points,
    final int dimension) {
    final Object[] pointArrary = new Object[points.getNumGeometries()];
    for (int i = 0; i < points.getNumGeometries(); i++) {
      final Point point = (Point)points.getGeometryN(i);
      final CoordinatesList coordinatesList = CoordinatesListUtil.get(point);
      final double[] coordinates = coordinatesList.getCoordinates();
      if (dimension == 3) {
        final double z = coordinates[2];
        if (Double.isNaN(z)) {
          coordinates[2] = 0;
        }
      }
      pointArrary[i] = coordinates;
    }
    final int srid = geometryFactory.getSRID();
    return JGeometry.createMultiPoint(pointArrary, dimension, srid);
  }

  private JGeometry toJGeometry(
    final MultiPolygon multiPolygon,
    final int dimension) {
    if (multiPolygon.getNumGeometries() == 1) {
      return toJGeometry((Polygon)multiPolygon.getGeometryN(0), dimension);
    } else {
      final int srid = geometryFactory.getSRID();
      int numCoordinates = 0;
      int numParts = 0;
      final List<double[][]> coordinateArraysList = new ArrayList<double[][]>();
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        final double[][] coordinateArrays = toCoordinateArrays(polygon,
          dimension);
        coordinateArraysList.add(coordinateArrays);
        numParts += coordinateArrays.length;
        for (int j = 0; j < coordinateArrays.length; j++) {
          final double[] coordinates = coordinateArrays[j];
          numCoordinates += coordinates.length;
        }
      }

      final int[] elemInfo = new int[numParts * 3];
      final double[] allCoordinates = new double[numCoordinates];
      int elementIndex = 0;
      int coordinateIndex = 0;
      for (final double[][] coordinateArrays : coordinateArraysList) {
        for (int i = 0; i < coordinateArrays.length; i++) {
          final double[] coordinates = coordinateArrays[i];
          System.arraycopy(coordinates, 0, allCoordinates, coordinateIndex,
            coordinates.length);
          elemInfo[elementIndex] = coordinateIndex + 1;
          if (i == 0) {
            elemInfo[elementIndex + 1] = 1003;
          } else {
            elemInfo[elementIndex + 1] = 2003;
          }
          elemInfo[elementIndex + 2] = 1;

          coordinateIndex += coordinates.length;
          elementIndex += 3;
        }
      }
      return new JGeometry(dimension * 1000 + 7, srid, elemInfo, allCoordinates);
    }
  }

  private JGeometry toJGeometry(
    final Point point,
    final int dimension) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(point);
    final int srid = geometryFactory.getSRID();
    final double x = coordinatesList.getX(0);
    final double y = coordinatesList.getY(0);
    if (dimension == 3) {
      double z = coordinatesList.getZ(0);
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
    final int dimension) {
    final Object[] oridinateArrays = toCoordinateArrays(polygon, dimension);
    final int srid = geometryFactory.getSRID();
    return JGeometry.createLinearPolygon(oridinateArrays, dimension, srid);
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

  private MultiPolygon toMultiPolygon(
    final ResultSet resultSet,
    final int columnIndex,
    final int numAxis)
    throws SQLException {
    List<Polygon> polygons = new ArrayList<Polygon>();

    final ARRAY elemInfoArray = (ARRAY)resultSet.getArray(columnIndex + 4);
    final long[] elemInfo = elemInfoArray.getLongArray();
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    LinearRing exteriorRing = null;
    List<LinearRing> interiorRings = null;
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
            if (exteriorRing != null) {
              final Polygon polygon = geometryFactory.createPolygon(
                exteriorRing, GeometryFactory.toLinearRingArray(interiorRings));
              polygons.add(polygon);
            }
            exteriorRing = ring;
            interiorRings = new ArrayList<LinearRing>();

          break;
          case 2003:
            interiorRings.add(ring);
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
    if (exteriorRing != null) {
      final Polygon polygon = geometryFactory.createPolygon(exteriorRing,
        GeometryFactory.toLinearRingArray(interiorRings));
      polygons.add(polygon);
    }

    return geometryFactory.createMultiPolygon(polygons);
  }
}
