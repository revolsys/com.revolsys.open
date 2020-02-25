package com.revolsys.geopackage.mil;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.schema.RecordStoreSchema;

import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;

public class GeoPackageFeatureTable extends GeoPackageRecordDefinition<FeatureDao> {

  public GeoPackageFeatureTable(final RecordStoreSchema schema, final FeatureDao featureDao) {
    super(schema, featureDao);

    addGeometryFields();
  }

  private void addGeometryFields() {
    final FeatureDao tableDao = getTableDao();
    final GeometryColumns geometryColumn = tableDao.getGeometryColumns();
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

      addField(fieldName, dataType);
      setGeometryFactory(geometryFactory);
    }
  }

}
