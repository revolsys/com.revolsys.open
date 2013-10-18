package com.revolsys.gis.oracle.esri;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreExtension;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.oracle.io.OracleDataObjectStore;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.DataStoreIteratorFactory;

public class ArcSdeSdeGeometryDataStoreExtension implements
  DataObjectStoreExtension {
  private static final ArcSdeSdeGeometryDataStoreExtension INSTANCE = new ArcSdeSdeGeometryDataStoreExtension();

  private static final DataStoreIteratorFactory ITERATOR_FACTORY = new DataStoreIteratorFactory(
    ArcSdeSdeGeometryDataStoreExtension.class, "createIterator");

  public static AbstractIterator<DataObject> createIterator(
    final OracleDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox == null) {
      return null;
    } else {
      return new ArcSdeSdeGeometryQueryIterator(dataStore, query, properties);
    }
  }

  public static ArcSdeSdeGeometryDataStoreExtension get() {
    return INSTANCE;
  }

  public ArcSdeSdeGeometryDataStoreExtension() {
  }

  @Override
  public void initialize(final DataObjectStore dataStore) {
  }

  @Override
  public boolean isEnabled(final DataObjectStore dataStore) {
    return ArcSdeConstants.isSdeAvailable(dataStore);
  }

  @Override
  public void postProcess(final DataObjectStoreSchema schema) {
    final AbstractJdbcDataObjectStore dataStore = (AbstractJdbcDataObjectStore)schema.getDataStore();
    for (final DataObjectMetaData metaData : schema.getTypes()) {
      final String typePath = metaData.getPath();
      final Map<String, Map<String, Object>> typeColumnProperties = ArcSdeConstants.getTypeColumnProperties(
        schema, typePath);
      for (final Entry<String, Map<String, Object>> columnEntry : typeColumnProperties.entrySet()) {
        final String columnName = columnEntry.getKey();
        final Map<String, Object> columnProperties = columnEntry.getValue();
        if (ArcSdeConstants.SDEBINARY.equals(columnProperties.get(ArcSdeConstants.GEOMETRY_COLUMN_TYPE))) {
          final ArcSdeSpatialReference spatialReference = (ArcSdeSpatialReference)columnProperties.get(ArcSdeConstants.SPATIAL_REFERENCE);
          final Attribute attribute = metaData.getAttribute(columnName);

          final int numAxis = ArcSdeConstants.getIntegerColumnProperty(schema,
            typePath, columnName, ArcSdeConstants.NUM_AXIS);
          if (numAxis == -1) {
            LoggerFactory.getLogger(getClass()).error(
              "Column not found in SDE.GEOMETRY_COLUMNS table " + metaData
                + "." + columnName);
          }
          final DataType dataType = ArcSdeConstants.getColumnProperty(schema,
            typePath, columnName, ArcSdeConstants.DATA_TYPE);
          if (dataType == null) {
            LoggerFactory.getLogger(getClass()).error(
              "Column not found in SDE.GEOMETRY_COLUMNS table " + metaData
                + "." + columnName);
          }
          final ArcSdeSdeGeometryAttribute sdeAttribute = new ArcSdeSdeGeometryAttribute(
            columnName, dataType, attribute.isRequired(),
            attribute.getProperties(), spatialReference, numAxis);
          ((DataObjectMetaDataImpl)metaData).replaceAttribute(attribute,
            sdeAttribute);
          metaData.setProperty("dataStoreIteratorFactory", ITERATOR_FACTORY);

          ArcSdeConstants.addObjectIdAttribute(dataStore, metaData);
        }
      }
    }
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
  }
}
