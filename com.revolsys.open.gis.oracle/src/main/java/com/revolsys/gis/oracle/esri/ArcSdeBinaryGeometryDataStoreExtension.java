package com.revolsys.gis.oracle.esri;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreExtension;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class ArcSdeBinaryGeometryDataStoreExtension implements
  DataObjectStoreExtension {

  private Object sdeUtil;

  public ArcSdeBinaryGeometryDataStoreExtension() {
  }

  @Override
  public void initialize(final DataObjectStore dataStore,
    final Map<String, Object> connectionProperties) {
    try {
      sdeUtil = new ArcSdeBinaryGeometryDataStoreUtil(dataStore,
        connectionProperties);
    } catch (final NoClassDefFoundError e) {

    }
  }

  @Override
  public boolean isEnabled(final DataObjectStore dataStore) {
    return ArcSdeConstants.isSdeAvailable(dataStore) && sdeUtil != null;
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
          if (sdeUtil == null) {
            LoggerFactory.getLogger(getClass())
              .error(
                "SDE Binary columns not supported without the ArcSDE Java API jars");
          } else {
            ((ArcSdeBinaryGeometryDataStoreUtil)sdeUtil).createGeometryColumn(
              dataStore, schema, metaData, typePath, columnName,
              columnProperties);
          }
        }
      }
    }
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
  }

}
