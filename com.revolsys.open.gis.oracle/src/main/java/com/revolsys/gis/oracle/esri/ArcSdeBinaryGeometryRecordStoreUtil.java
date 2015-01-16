package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.oracle.io.OracleRecordStore;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.attribute.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public class ArcSdeBinaryGeometryRecordStoreUtil {

  private final RecordStoreIteratorFactory iteratorFactory = new RecordStoreIteratorFactory(
    this, "createIterator");

  private Map<String, Object> connectionProperties = new HashMap<String, Object>();

  private JdbcRecordStore recordStore;

  public ArcSdeBinaryGeometryRecordStoreUtil() {
  }

  public ArcSdeBinaryGeometryRecordStoreUtil(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
    this.recordStore = (JdbcRecordStore)recordStore;
    this.connectionProperties = connectionProperties;
  }

  public SeConnection close(final SeConnection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (final SeException e) {
      }
    }
    return null;
  }

  public SeQuery close(final SeQuery query) {
    if (query != null) {
      try {
        query.close();
      } catch (final SeException e) {
      }
    }
    return null;
  }

  public void createGeometryColumn(final AbstractJdbcRecordStore recordStore,
    final RecordStoreSchema schema, final RecordDefinition recordDefinition,
    final String typePath, final String columnName,
    final Map<String, Object> columnProperties) {
    final FieldDefinition attribute = recordDefinition.getField(columnName);

    DataType dataType = JdbcFieldAdder.getColumnProperty(schema, typePath,
      columnName, JdbcFieldAdder.GEOMETRY_TYPE);
    if (dataType == null) {
      dataType = DataTypes.GEOMETRY;
    }

    GeometryFactory geometryFactory = JdbcFieldAdder.getColumnProperty(
      schema, typePath, columnName, JdbcFieldAdder.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = schema.getGeometryFactory();
    }

    final ArcSdeBinaryGeometryFieldDefinition sdeAttribute = new ArcSdeBinaryGeometryFieldDefinition(
      this, columnName, columnName, dataType, attribute.isRequired(),
      "The GEOMETRY reference", attribute.getProperties(), geometryFactory);
    ((RecordDefinitionImpl)recordDefinition).replaceField(attribute,
      sdeAttribute);
    sdeAttribute.setRecordDefinition(recordDefinition);

    recordDefinition.setProperty("recordStoreIteratorFactory",
      this.iteratorFactory);

    ((RecordDefinitionImpl)recordDefinition).setGeometryFieldName(columnName);
  }

  public AbstractIterator<Record> createIterator(
    final OracleRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    final BoundingBox boundingBox = QueryValue.getBoundingBox(query);
    if (boundingBox == null) {
      return null;
    } else {
      return new ArcSdeBinaryGeometryQueryIterator(this, recordStore, query,
        properties);
    }
  }

  public SeConnection createSeConnection() {
    final String server = (String)this.connectionProperties.get("sdeServer");

    if (!Property.hasValue(server)) {
      throw new IllegalArgumentException(
          "The connection properties must include a sdeServer to support ESRI ArcSDE SDEBINARY columns");
    }
    String instance = (String)this.connectionProperties.get("sdeInstance");
    if (!Property.hasValue(instance)) {
      instance = "5151";
    }
    String database = (String)this.connectionProperties.get("sdeDatabase");
    if (!Property.hasValue(database)) {
      database = "none";
    }
    final String username = (String)this.connectionProperties.get("username");
    String password = (String)this.connectionProperties.get("password");
    password = PasswordUtil.decrypt(password);

    try {
      return new SeConnection(server, instance, database, username, password);
    } catch (final SeException e) {
      throw new RuntimeException("Unable to create connection", e);
    }
  }

  public LineString getCoordinates(final SeShape shape,
    final double[][][] allCoordinates, final int partIndex,
    final int ringIndex, final int axisCount) {
    try {
      final int numCoords = shape.getNumPoints(partIndex + 1, ringIndex + 1);
      final double[] coordinates = new double[numCoords * axisCount];
      for (int coordinateIndex = 0; coordinateIndex < numCoords; coordinateIndex++) {

        final double x = allCoordinates[partIndex][ringIndex][coordinateIndex
                                                              * axisCount];
        final double y = allCoordinates[partIndex][ringIndex][coordinateIndex
                                                              * axisCount + 1];
        CoordinatesListUtil.setCoordinates(coordinates, axisCount,
          coordinateIndex, x, y);
      }
      return new LineStringDouble(axisCount, coordinates);
    } catch (final SeException e) {
      throw new RuntimeException("Unable to get coordinates", e);
    }
  }

  public String getTableName(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    return this.recordStore.getDatabaseQualifiedTableName(typePath);
  }

  public void setValueFromRow(final Record object, final SeRow row,
    final int columnIndex) {
    if (object != null && row != null) {
      try {
        final SeColumnDefinition columnDefinition = row.getColumnDef(columnIndex);
        final int type = columnDefinition.getType();
        if (row.getIndicator(columnIndex) != SeRow.SE_IS_NULL_VALUE) {

          final String name = columnDefinition.getName();
          Object value = null;
          switch (type) {
            case SeColumnDefinition.TYPE_INT16:
              value = row.getShort(columnIndex);
              break;

            case SeColumnDefinition.TYPE_INT32:
              value = row.getInteger(columnIndex);
              break;

            case SeColumnDefinition.TYPE_INT64:
              value = row.getLong(columnIndex);
              break;

            case SeColumnDefinition.TYPE_FLOAT32:
              value = row.getFloat(columnIndex);
              break;

            case SeColumnDefinition.TYPE_FLOAT64:
              value = row.getDouble(columnIndex);
              break;

            case SeColumnDefinition.TYPE_STRING:
              value = row.getString(columnIndex);
              break;

            case SeColumnDefinition.TYPE_NSTRING:
              value = row.getNString(columnIndex);
              break;

            case SeColumnDefinition.TYPE_CLOB:
              final ByteArrayInputStream clob = row.getClob(columnIndex);
              value = FileUtil.getString(clob);
              break;
            case SeColumnDefinition.TYPE_NCLOB:
              final ByteArrayInputStream nClob = row.getNClob(columnIndex);
              value = FileUtil.getString(nClob);
              break;

            case SeColumnDefinition.TYPE_XML:
              value = row.getXml(columnIndex).getText();
              break;

            case SeColumnDefinition.TYPE_UUID:
              value = row.getUuid(columnIndex);
              break;

            case SeColumnDefinition.TYPE_DATE:
              value = row.getTime(columnIndex);
              break;

            case SeColumnDefinition.TYPE_SHAPE:
              final SeShape shape = row.getShape(columnIndex);
              value = toGeometry(shape);
              break;

            default:
              LoggerFactory.getLogger(ArcSdeBinaryGeometryRecordStoreUtil.class)
              .error(
                "Unsupported column type: " + object.getRecordDefinition()
                + "." + name);
              break;
          }
          object.setValue(name, value);
        }
      } catch (final SeException e) {
        throw new RuntimeException("Unable to get value " + columnIndex
          + " from result set", e);
      }
    }
  }

  public Geometry toGeometry(final SeShape shape) {
    try {
      final int type = shape.getType();
      final SeCoordinateReference coordRef = shape.getCoordRef();
      final int srid = (int)coordRef.getSrid().longValue();
      final double scaleXy = coordRef.getXYUnits();
      final double scaleZ = coordRef.getZUnits();
      int axisCount = 2;
      if (shape.is3D()) {
        axisCount = 3;
      }
      if (shape.isMeasured()) {
        axisCount = 4;
      }
      final GeometryFactory geometryFactory = GeometryFactory.fixed(srid,
        axisCount, scaleXy, scaleZ);

      final int numParts = shape.getNumParts();
      final double[][][] allCoordinates = shape.getAllCoords();
      switch (type) {

        case SeShape.TYPE_NIL:
          return geometryFactory.geometry();
        case SeShape.TYPE_POINT:
        case SeShape.TYPE_MULTI_POINT:
          final List<Point> points = new ArrayList<Point>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final LineString coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, axisCount);
              final Point point = geometryFactory.point(coordinates);
              if (!point.isEmpty()) {
                points.add(point);
              }
            }
          }
          if (points.size() == 1) {
            return points.get(0);
          } else {
            return geometryFactory.multiPoint(points);
          }
        case SeShape.TYPE_MULTI_LINE:
        case SeShape.TYPE_LINE:
          final List<LineString> lines = new ArrayList<LineString>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final LineString coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, axisCount);
              final LineString line = geometryFactory.lineString(coordinates);
              if (!line.isEmpty()) {
                lines.add(line);
              }
            }
          }
          if (lines.size() == 1) {
            return lines.get(0);
          } else {
            return geometryFactory.multiLineString(lines);
          }
        case SeShape.TYPE_POLYGON:
        case SeShape.TYPE_MULTI_POLYGON:
          final List<Polygon> polygons = new ArrayList<Polygon>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            final List<LineString> rings = new ArrayList<LineString>();
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final LineString coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, axisCount);
              rings.add(coordinates);
            }
            if (!rings.isEmpty()) {
              final Polygon polygon = geometryFactory.polygon(rings);
              polygons.add(polygon);
            }
          }
          if (polygons.size() == 1) {
            return polygons.get(0);
          } else {
            return geometryFactory.multiPolygon(polygons);
          }

        default:
          throw new IllegalArgumentException("Shape not supported:"
              + shape.asText(1000));
      }

    } catch (final SeException e) {
      throw new RuntimeException("Unable to read shape", e);
    }
  }
}
