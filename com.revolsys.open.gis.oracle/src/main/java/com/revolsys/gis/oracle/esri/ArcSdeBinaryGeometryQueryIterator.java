package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeEnvelope;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.util.ExceptionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ArcSdeBinaryGeometryQueryIterator extends
  AbstractIterator<DataObject> {

  private static SeConnection sdeConnection;

  private DataObjectFactory dataObjectFactory;

  private JdbcDataObjectStore dataStore;

  private DataObjectMetaData metaData;

  private SeQuery seQuery;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Query query;

  private Statistics statistics;

  private final ArcSdeBinaryGeometryDataStoreExtension extension;

  public ArcSdeBinaryGeometryQueryIterator(
    final ArcSdeBinaryGeometryDataStoreExtension extension,
    final JdbcDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    this.extension = extension;
    this.dataObjectFactory = query.getProperty("dataObjectFactory");
    if (this.dataObjectFactory == null) {
      this.dataObjectFactory = dataStore.getDataObjectFactory();
    }
    this.dataStore = dataStore;
    this.query = query;
    this.statistics = (Statistics)properties.get(Statistics.class.getName());
  }

  private void closeSeQuery() {
    try {
      if (this.seQuery != null) {
        this.seQuery.close();
        this.seQuery = null;
      }
    } catch (final SeException e) {
      LoggerFactory.getLogger(ArcSdeBinaryGeometryQueryIterator.class).error(
        "Unable to close query", e);
    }
  }

  @Override
  @PreDestroy
  public void doClose() {
    try {
      closeSeQuery();
    } finally {
      try {
        if (sdeConnection != null) {
          sdeConnection.close();
          sdeConnection = null;
        }
      } catch (final SeException e) {
        ExceptionUtil.log(getClass(), e);
      } finally {

        this.attributes = null;
        this.dataObjectFactory = null;
        this.dataStore = null;
        this.metaData = null;
        this.query = null;
        this.seQuery = null;
        this.statistics = null;
      }
    }
  }

  @Override
  protected void doInit() {
    String tableName = this.dataStore.getDatabaseQualifiedTableName(this.query.getTypeName());
    this.metaData = this.query.getMetaData();
    if (this.metaData == null) {
      if (tableName != null) {
        this.metaData = this.dataStore.getMetaData(tableName);
        this.query.setMetaData(this.metaData);

      }
    }
    if (this.metaData != null) {
      tableName = this.dataStore.getDatabaseQualifiedTableName(this.metaData.getPath());
    }
    try {

      final List<String> attributeNames = new ArrayList<String>(
        this.query.getAttributeNames());
      if (attributeNames.isEmpty()) {
        this.attributes.addAll(this.metaData.getAttributes());
        attributeNames.addAll(this.metaData.getAttributeNames());
      } else {
        for (final String attributeName : attributeNames) {
          if (attributeName.equals("*")) {
            this.attributes.addAll(this.metaData.getAttributes());
            attributeNames.addAll(this.metaData.getAttributeNames());
          } else {
            final Attribute attribute = this.metaData.getAttribute(attributeName);
            if (attribute != null) {
              this.attributes.add(attribute);
            }
            attributeNames.add(attributeName);
          }
        }
      }

      sdeConnection = this.extension.createSeConnection();
      final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
      final String[] columnNames = attributeNames.toArray(new String[0]);
      this.seQuery = new SeQuery(sdeConnection, columnNames, sqlConstruct);
      BoundingBox boundingBox = this.query.getBoundingBox();
      if (boundingBox != null) {
        final SeLayer layer = new SeLayer(sdeConnection, tableName,
          this.metaData.getGeometryAttributeName());

        final GeometryFactory geometryFactory = this.metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final SeEnvelope envelope = new SeEnvelope(boundingBox.getMinX(),
          boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY());
        final SeShape shape = new SeShape(layer.getCoordRef());
        shape.generateRectangle(envelope);
        final SeShapeFilter filter = new SeShapeFilter(tableName,
          this.metaData.getGeometryAttributeName(), shape, SeFilter.METHOD_ENVP);
        this.seQuery.setSpatialConstraints(SeQuery.SE_SPATIAL_FIRST, false,
          new SeFilter[] {
            filter
          });
      }
      // TODO where clause
      // TODO how to load geometry for non-spatial queries
      this.seQuery.prepareQuery();
      this.seQuery.execute();

      final String typePath = this.query.getTypeNameAlias();
      if (typePath != null) {
        final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl)this.metaData).clone();
        newMetaData.setName(typePath);
        this.metaData = newMetaData;
      }
    } catch (final SeException e) {
      closeSeQuery();
      throw new RuntimeException("Error performing query", e);
    }
  }

  private CoordinatesList getCoordinates(final SeShape shape,
    final double[][][] allCoordinates, final int partIndex,
    final int ringIndex, final int numAxis) throws SeException {
    final int numCoords = shape.getNumPoints(partIndex + 1, ringIndex + 1);
    final CoordinatesList coordinates = new DoubleCoordinatesList(numCoords,
      numAxis);
    for (int coordinateIndex = 0; coordinateIndex < numCoords; coordinateIndex++) {

      final double x = allCoordinates[partIndex][ringIndex][coordinateIndex
        * numAxis];
      final double y = allCoordinates[partIndex][ringIndex][coordinateIndex
        * numAxis + 1];
      coordinates.setX(coordinateIndex, x);
      coordinates.setY(coordinateIndex, y);
    }
    return coordinates;
  }

  public DataObjectMetaData getMetaData() {
    if (this.metaData == null) {
      hasNext();
    }
    return this.metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    try {
      if (this.seQuery != null) {
        final SeRow row = this.seQuery.fetch();
        if (row != null) {
          final DataObject object = getNextRecord(row);
          if (this.statistics != null) {
            this.statistics.add(object);
          }
          return object;
        }
      }
      close();
      throw new NoSuchElementException();
    } catch (final SeException e) {
      close();
      throw new RuntimeException(this.query.getSql(), e);
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  private DataObject getNextRecord(

  final SeRow row) {
    final DataObject object = this.dataObjectFactory.createDataObject(this.metaData);
    if (object != null) {
      object.setState(DataObjectState.Initalizing);
      for (int columnIndex = 0; columnIndex < this.attributes.size(); columnIndex++) {
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
                LoggerFactory.getLogger(getClass()).error(
                  "Unsupported column type: " + metaData + "." + name);
              break;
            }
            object.setValue(name, value);
          }
        } catch (final SeException e) {
          throw new RuntimeException("Unable to get value " + columnIndex
            + " from result set", e);
        }
      }
      object.setState(DataObjectState.Persisted);
      this.dataStore.addStatistic("query", object);
    }
    return object;
  }

  private Geometry toGeometry(final SeShape shape) {

    try {
      final int type = shape.getType();
      final SeCoordinateReference coordRef = shape.getCoordRef();
      final int srid = (int)coordRef.getSrid().longValue();
      final double scaleXy = coordRef.getXYUnits();
      final double scaleZ = coordRef.getZUnits();
      int numAxis = 2;
      if (shape.is3D()) {
        numAxis = 3;
      }
      if (shape.isMeasured()) {
        numAxis = 4;
      }
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid,
        numAxis, scaleXy, scaleZ);

      final int numParts = shape.getNumParts();
      final double[][][] allCoordinates = shape.getAllCoords();
      switch (type) {

        case SeShape.TYPE_NIL:
          return geometryFactory.createEmptyGeometry();
        case SeShape.TYPE_POINT:
        case SeShape.TYPE_MULTI_POINT:
          final List<Point> points = new ArrayList<Point>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, numAxis);
              final Point point = geometryFactory.createPoint(coordinates);
              if (!point.isEmpty()) {
                points.add(point);
              }
            }
          }
          if (points.size() == 1) {
            return points.get(0);
          } else {
            return geometryFactory.createMultiPoint(points);
          }
        case SeShape.TYPE_MULTI_LINE:
        case SeShape.TYPE_LINE:
          final List<LineString> lines = new ArrayList<LineString>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, numAxis);
              final LineString line = geometryFactory.createLineString(coordinates);
              if (!line.isEmpty()) {
                lines.add(line);
              }
            }
          }
          if (lines.size() == 1) {
            return lines.get(0);
          } else {
            return geometryFactory.createMultiLineString(lines);
          }
        case SeShape.TYPE_POLYGON:
        case SeShape.TYPE_MULTI_POLYGON:
          final List<Polygon> polygons = new ArrayList<Polygon>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape,
                allCoordinates, partIndex, ringIndex, numAxis);
              rings.add(coordinates);
            }
            if (!rings.isEmpty()) {
              final Polygon polygon = geometryFactory.createPolygon(rings);
              polygons.add(polygon);
            }
          }
          if (polygons.size() == 1) {
            return polygons.get(0);
          } else {
            return geometryFactory.createMultiPolygon(polygons);
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
