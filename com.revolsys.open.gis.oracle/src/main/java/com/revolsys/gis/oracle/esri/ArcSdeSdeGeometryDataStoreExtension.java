package com.revolsys.gis.oracle.esri;

import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreExtension;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class ArcSdeSdeGeometryDataStoreExtension implements
  DataObjectStoreExtension {
  private static final ArcSdeSdeGeometryDataStoreExtension INSTANCE = new ArcSdeSdeGeometryDataStoreExtension();

  public static ArcSdeSdeGeometryDataStoreExtension get() {
    return INSTANCE;
  }

  public ArcSdeSdeGeometryDataStoreExtension() {
  }

  @Override
  public boolean initialize(final DataObjectStore dataStore) {
    return isEnabled(dataStore);
  }

  @Override
  public boolean isEnabled(final DataObjectStore dataStore) {
    return ArcSdeConstants.isSdeAvailable(dataStore);
  }

  @Override
  public void postProcess(final DataObjectStoreSchema schema) {
    for (final DataObjectMetaData metaData : schema.getTypes()) {
      final String typePath = metaData.getPath();
      final Map<String, Map<String, Object>> typeColumnProperties = ArcSdeConstants.getTypeColumnProperties(
        schema, typePath);
      for (final Entry<String, Map<String, Object>> columnEntry : typeColumnProperties.entrySet()) {
        final String columnName = columnEntry.getKey();
        final Map<String, Object> columnProperties = columnEntry.getValue();
        if (ArcSdeConstants.SDEBINARY.equals(columnProperties.get(ArcSdeConstants.GEOMETRY_COLUMN_TYPE))) {
          System.out.println(columnName + "=" + columnProperties);
        }
      }
    }
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
  }
}
