package com.revolsys.gis.oracle.esri;

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
  public void initialize(final DataObjectStore dataStore) {
  }

  @Override
  public void postProcess(final DataObjectStoreSchema schema) {
    for (final DataObjectMetaData metaData : schema.getTypes()) {

    }
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
  }
}
