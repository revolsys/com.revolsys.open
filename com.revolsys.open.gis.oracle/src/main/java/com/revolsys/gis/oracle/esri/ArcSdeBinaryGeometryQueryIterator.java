package com.revolsys.gis.oracle.esri;

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
import com.revolsys.jdbc.JdbcUtils;
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

  private void closeSeQuery(final SeQuery seQuery) {
    try {
      if (seQuery != null) {
        seQuery.close();
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
      if (sdeConnection != null) {
        sdeConnection.close();
        sdeConnection = null;
      }
    } catch (final SeException e) {
      ExceptionUtil.log(getClass(), e);
    } finally {
      closeSeQuery(seQuery);
      attributes = null;
      dataObjectFactory = null;
      dataStore = null;
      metaData = null;
      query = null;
      seQuery = null;
      statistics = null;
    }
  }

  @Override
  protected void doInit() {
    String tableName = query.getTypeName();
    metaData = query.getMetaData();
    if (metaData == null) {
      if (tableName != null) {
        metaData = dataStore.getMetaData(tableName);
        query.setMetaData(metaData);

      }
    }
    tableName = dataStore.getDatabaseTableName(metaData.getPath());
    try {

      final List<String> attributeNames = new ArrayList<String>(
        query.getAttributeNames());
      if (attributeNames.isEmpty()) {
        this.attributes.addAll(metaData.getAttributes());
      } else {
        for (final String attributeName : attributeNames) {
          if (attributeName.equals("*")) {
            this.attributes.addAll(metaData.getAttributes());
          } else {
            final Attribute attribute = metaData.getAttribute(attributeName);
            if (attribute != null) {
              attributes.add(attribute);
            }
          }
        }
      }

      sdeConnection = extension.createSeConnection();
      final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
      seQuery = new SeQuery(sdeConnection,
        attributeNames.toArray(new String[0]), sqlConstruct);
      BoundingBox boundingBox = query.getBoundingBox();
      if (boundingBox != null) {
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final SeEnvelope envelope = new SeEnvelope(boundingBox.getMinX(),
          boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY());
        final SeShape shape = new SeShape();
        shape.generateRectangle(envelope);
        new SeShapeFilter(tableName, metaData.getGeometryAttributeName(),
          shape, SeFilter.METHOD_ENVP);
      }
      // TODO where clause
      // TODO how to load geometry for non-spatial queries
      seQuery.prepareQuery();
      seQuery.execute();

      final String typePath = query.getTypeNameAlias();
      if (typePath != null) {
        final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl)metaData).clone();
        newMetaData.setName(typePath);
        this.metaData = newMetaData;
      }
    } catch (final SeException e) {
      closeSeQuery(seQuery);
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

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  public DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    try {
      if (seQuery != null) {
        final SeRow row = seQuery.fetch();
        if (row != null) {
          final DataObject object = getNextObject(row);
          if (statistics != null) {
            statistics.add(object);
          }
          return object;
        }
      }
      close();
      throw new NoSuchElementException();
    } catch (final SeException e) {
      close();
      throw new RuntimeException(query.getSql(), e);
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  private DataObject getNextObject(

  final SeRow row) {
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    if (object != null) {
      object.setState(DataObjectState.Initalizing);
      for (int columnIndex = 0; columnIndex < attributes.size(); columnIndex++) {
        try {
          final SeColumnDefinition columnDefinition = row.getColumnDef(columnIndex);
          final int type = columnDefinition.getType();
          if (row.getIndicator(columnIndex) != SeRow.SE_IS_NULL_VALUE) {

            final String name = columnDefinition.getName();
            switch (type) {

              case SeColumnDefinition.TYPE_INT16:
                object.setValue(name, row.getShort(columnIndex));
              break;

              case SeColumnDefinition.TYPE_DATE:
                object.setValue(name, row.getTime(columnIndex));
              break;

              case SeColumnDefinition.TYPE_INT32:
                object.setValue(name, row.getInteger(columnIndex));
              break;

              case SeColumnDefinition.TYPE_FLOAT32:
                object.setValue(name, row.getFloat(columnIndex));
              break;

              case SeColumnDefinition.TYPE_FLOAT64:
                object.setValue(name, row.getDouble(columnIndex));
              break;

              case SeColumnDefinition.TYPE_STRING:
                object.setValue(name, row.getString(columnIndex));
              break;

              case SeColumnDefinition.TYPE_SHAPE:
                final SeShape shape = row.getShape(columnIndex);
                object.setValue(name, toGeometry(shape));
              break;
            }
          }
        } catch (final SeException e) {
          throw new RuntimeException("Unable to get value " + columnIndex
            + " from result set", e);
        }
        columnIndex++;
      }
      object.setState(DataObjectState.Persisted);
      dataStore.addStatistic("query", object);
    }
    return object;
  }

  protected String getSql(final Query query) {
    return JdbcUtils.getSelectSql(query);
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
