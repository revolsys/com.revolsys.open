package com.revolsys.geopackage;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.table.Constraint;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.manager.GeoPackageManager;

public class GeoPackageRecordStore extends AbstractRecordStore {

  private final File file;

  private boolean createMissingRecordStore = false;

  private GeoPackage geoPackage;

  public GeoPackageRecordStore(final File file) {
    this.file = file;
    setConnectionProperties(Collections.singletonMap("url", FileUtil.toUrl(file).toString()));
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      final GeoPackage geoPackage = this.geoPackage;
      this.geoPackage = null;
      if (geoPackage != null) {
        geoPackage.close();
      }
    }
  }

  @Override
  public int getRecordCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getRecordStoreType() {
    return GeoPackageFactory.DESCRIPTION;
  }

  @Override
  protected void initializeDo() {
    if (!this.file.exists()) {
      if (this.createMissingRecordStore) {
        FileUtil.createParentDirectories(this.file);
        if (!GeoPackageManager.create(this.file)) {
          throw new IllegalStateException("Unable to create GeoPackage: " + this.file);
        }
      } else {
        throw new IllegalStateException("GeoPackage not found : " + this.file);
      }
    }
    this.geoPackage = GeoPackageManager.open(this.file);
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    synchronized (schema) {
      final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
      for (final String tableName : this.geoPackage.getTables()) {
        if (this.geoPackage.isFeatureTable(tableName)) {
          final FeatureDao featureDao = this.geoPackage.getFeatureDao(tableName);
          final RecordDefinitionBuilder recordDefinition = new RecordDefinitionBuilder(schema,
            tableName);

          final FeatureTable table = featureDao.getTable();
          for (final FeatureColumn column : table.getColumns()) {
            final String fieldName = column.getName();
            final GeoPackageDataType geoPackageDataType = column.getDataType();
            DataType dataType = null;
            switch (geoPackageDataType) {
              case BLOB:
                dataType = DataTypes.BLOB;
              break;
              case BOOLEAN:
                dataType = DataTypes.BOOLEAN;
              break;
              case DATE:
                dataType = DataTypes.DATE;
              break;
              case DATETIME:
                dataType = DataTypes.DATE_TIME;
              break;
              case DOUBLE:
                dataType = DataTypes.DOUBLE;
              break;
              case FLOAT:
                dataType = DataTypes.FLOAT;
              break;
              case INT:
                dataType = DataTypes.LONG;
              break;
              case INTEGER:
                dataType = DataTypes.LONG;
              break;
              case MEDIUMINT:
                dataType = DataTypes.INT;
              break;
              case REAL:
                dataType = DataTypes.DOUBLE;
              break;
              case SMALLINT:
                dataType = DataTypes.SHORT;
              break;
              case TEXT:
                dataType = DataTypes.STRING;
              break;
              case TINYINT:
                dataType = DataTypes.BYTE;
              break;

              default:
                dataType = DataTypes.OBJECT;
              break;
            }
            boolean required = false;
            for (final Constraint constraint : column.getConstraints()) {
              switch (constraint.getType()) {
                case NOT_NULL:
                  required = true;
                break;
                default:
                break;
              }
            }
            recordDefinition.addField(fieldName, dataType, required);
          }
          final GeometryColumns geometryColumn = featureDao.getGeometryColumns();
          if (geometryColumn != null) {
            final String fieldName = geometryColumn.getColumnName();
            final String geometryTypeName = geometryColumn.getGeometryTypeName();
            final DataType dataType = DataTypes.getDataType(geometryTypeName);
            int axisCount = 2;
            if (geometryColumn.getM() == 1) {
              axisCount = 4;
            } else if (geometryColumn.getZ() == 1) {
              axisCount = 3;
            }
            final int coordinateSystemId = (int)geometryColumn.getSrsId();
            final GeometryFactory geometryFactory = GeometryFactory.floating(coordinateSystemId,
              axisCount);
            recordDefinition//
              .addField(fieldName, dataType) //
              .setGeometryFactory(geometryFactory) //
            ;
          }

          final PathName pathName = recordDefinition.getPathName();
          elementsByPath.put(pathName, recordDefinition.getRecordDefinition());
        }
      }

      return elementsByPath;
    }
  }

  public GeoPackageRecordStore setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
    return this;
  }

}
