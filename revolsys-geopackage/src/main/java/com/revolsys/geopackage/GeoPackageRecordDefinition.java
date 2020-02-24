package com.revolsys.geopackage;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserColumn;
import mil.nga.geopackage.user.UserDao;
import mil.nga.geopackage.user.UserTable;

public class GeoPackageRecordDefinition<DAO extends UserDao<?, ?, ?, ?>>
  extends RecordDefinitionImpl {

  private static DataType getDataType(final GeoPackageDataType geoPackageDataType) {
    DataType dataType = null;
    switch (geoPackageDataType) {
      case BOOLEAN:
        dataType = DataTypes.BOOLEAN;
      break;
      case TINYINT:
        dataType = DataTypes.BYTE;
      break;
      case SMALLINT:
        dataType = DataTypes.SHORT;
      break;
      case MEDIUMINT:
        dataType = DataTypes.INT;
      break;
      case INT:
      case INTEGER:
        dataType = DataTypes.LONG;
      break;
      case FLOAT:
        dataType = DataTypes.FLOAT;
      break;
      case DOUBLE:
      case REAL:
        dataType = DataTypes.DOUBLE;
      break;
      case TEXT:
        dataType = DataTypes.STRING;
      break;
      case BLOB:
        dataType = DataTypes.BLOB;
      break;
      case DATE:
        dataType = DataTypes.DATE;
      break;
      case DATETIME:
        dataType = DataTypes.DATE_TIME;
      break;

      default:
        dataType = DataTypes.OBJECT;
      break;
    }
    return dataType;
  }

  private final DAO tableDao;

  public GeoPackageRecordDefinition(final RecordStoreSchema schema, final DAO userDao) {
    super(schema, PathName.newPathName(userDao.getTableName()));
    this.tableDao = userDao;

    addFields();
  }

  private void addFields() {
    final UserTable<?> table = this.tableDao.getTable();
    for (final UserColumn column : table.getColumns()) {
      final FieldDefinition field = new FieldDefinition();

      final String fieldName = column.getName();
      field.setName(fieldName);

      if (column.isNotNull()) {
        field.setRequired(true);
      }

      final GeoPackageDataType geoPackageDataType = column.getDataType();
      final DataType dataType = getDataType(geoPackageDataType);
      field.setType(dataType);

      if (column.hasMax()) {
        final int length = column.getMax().intValue();
        field.setLength(length);
      }

      if (column.hasDefaultValue()) {
        final Object defaultValue = column.getDefaultValue();
        field.setDefaultValue(defaultValue);
      }

      addField(field);
      if (column.isPrimaryKey()) {
        addIdField(field);
      }
    }
  }

  public DAO getTableDao() {
    return this.tableDao;
  }

}
