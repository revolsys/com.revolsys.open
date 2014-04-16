package com.revolsys.gis.oracle.esri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.esri.sde.sdk.client.SeConnection;
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
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.jdbc.io.JdbcDataObjectStore;

public class ArcSdeBinaryGeometryQueryIterator extends
  AbstractIterator<DataObject> {

  private SeConnection connection;

  private DataObjectFactory dataObjectFactory;

  private JdbcDataObjectStore dataStore;

  private DataObjectMetaData metaData;

  private SeQuery seQuery;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Query query;

  private Statistics statistics;

  private ArcSdeBinaryGeometryDataStoreUtil sdeUtil;

  public ArcSdeBinaryGeometryQueryIterator(
    final ArcSdeBinaryGeometryDataStoreUtil sdeUtil,
    final JdbcDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    this.sdeUtil = sdeUtil;
    this.dataObjectFactory = query.getProperty("dataObjectFactory");
    if (this.dataObjectFactory == null) {
      this.dataObjectFactory = dataStore.getDataObjectFactory();
    }
    this.dataStore = dataStore;
    this.query = query;
    this.statistics = (Statistics)properties.get(Statistics.class.getName());
  }

  @Override
  @PreDestroy
  public void doClose() {
    if (sdeUtil != null) {
      try {
        this.seQuery = sdeUtil.close(seQuery);
      } finally {
        this.connection = sdeUtil.close(connection);
      }
    }
    this.sdeUtil = null;
    this.attributes = null;
    this.dataObjectFactory = null;
    this.dataStore = null;
    this.metaData = null;
    this.query = null;
    this.seQuery = null;
    this.statistics = null;
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
      tableName = sdeUtil.getTableName(this.metaData);
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

      connection = this.sdeUtil.createSeConnection();
      final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
      final String[] columnNames = attributeNames.toArray(new String[0]);
      this.seQuery = new SeQuery(connection, columnNames, sqlConstruct);
      BoundingBox boundingBox = this.query.getBoundingBox();
      if (boundingBox != null) {
        final SeLayer layer = new SeLayer(connection, tableName,
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
      this.seQuery = sdeUtil.close(seQuery);
      throw new RuntimeException("Error performing query", e);
    }
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
          final DataObject object = getNextRecord(metaData, row);
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

  private DataObject getNextRecord(final DataObjectMetaData metaData,
    final SeRow row) {
    final DataObject object = this.dataObjectFactory.createDataObject(metaData);
    if (object != null) {
      object.setState(DataObjectState.Initalizing);
      for (int columnIndex = 0; columnIndex < this.attributes.size(); columnIndex++) {
        sdeUtil.setValueFromRow(object, row, columnIndex);
      }
      object.setState(DataObjectState.Persisted);
      this.dataStore.addStatistic("query", object);
    }
    return object;
  }

}
