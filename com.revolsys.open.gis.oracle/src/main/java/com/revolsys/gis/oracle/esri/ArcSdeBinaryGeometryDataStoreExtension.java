package com.revolsys.gis.oracle.esri;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreExtension;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.oracle.io.OracleSdoGeometryJdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;

public class ArcSdeBinaryGeometryDataStoreExtension implements
  RecordStoreExtension {

  private Object sdeUtil;

  public ArcSdeBinaryGeometryDataStoreExtension() {
  }

  @Override
  public void initialize(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
    try {
      sdeUtil = new ArcSdeBinaryGeometryDataStoreUtil(recordStore,
        connectionProperties);
    } catch (final NoClassDefFoundError e) {

    }
  }

  @Override
  public boolean isEnabled(final RecordStore recordStore) {
    return ArcSdeConstants.isSdeAvailable(recordStore) && sdeUtil != null;
  }

  @Override
  public void postProcess(final RecordStoreSchema schema) {
    final AbstractJdbcRecordStore recordStore = (AbstractJdbcRecordStore)schema.getDataStore();
    for (final RecordDefinition recordDefinition : schema.getTypes()) {
      final String typePath = recordDefinition.getPath();
      final Map<String, Map<String, Object>> typeColumnProperties = JdbcAttributeAdder.getTypeColumnProperties(
        schema, typePath);
      for (final Entry<String, Map<String, Object>> columnEntry : typeColumnProperties.entrySet()) {
        final String columnName = columnEntry.getKey();
        final Map<String, Object> columnProperties = columnEntry.getValue();
        if (ArcSdeConstants.SDEBINARY.equals(columnProperties.get(ArcSdeConstants.GEOMETRY_COLUMN_TYPE))) {
          final Attribute attribute = recordDefinition.getAttribute(columnName);
          if (!(attribute instanceof OracleSdoGeometryJdbcAttribute)) {
            if (sdeUtil == null) {
              LoggerFactory.getLogger(getClass())
                .error(
                  "SDE Binary columns not supported without the ArcSDE Java API jars");
            } else {
              ((ArcSdeBinaryGeometryDataStoreUtil)sdeUtil).createGeometryColumn(
                recordStore, schema, recordDefinition, typePath, columnName,
                columnProperties);
            }
          }
        }
      }
    }
  }

  @Override
  public void preProcess(final RecordStoreSchema schema) {
  }

}
