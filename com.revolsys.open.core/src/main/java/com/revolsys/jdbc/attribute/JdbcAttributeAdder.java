package com.revolsys.jdbc.attribute;

import java.sql.Types;

import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;

public class JdbcAttributeAdder {
  private DataType dataType;

  public JdbcAttributeAdder() {
  }

  public JdbcAttributeAdder(final DataType dataType) {
    this.dataType = dataType;
  }

  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final int sqlType, int length, int scale,
    final boolean required) {
    JdbcAttribute attribute;
    switch (sqlType) {
      case Types.CHAR:
      case Types.CLOB:
      case Types.LONGVARCHAR:
      case Types.VARCHAR:
        attribute = new JdbcStringAttribute(name, sqlType, length, required,
          null);
      break;
      case Types.BIGINT:
        attribute = new JdbcLongAttribute(name, sqlType, length, required, null);
      break;
      case Types.INTEGER:
        attribute = new JdbcIntegerAttribute(name, sqlType, length, required,
          null);
      break;
      case Types.SMALLINT:
        attribute = new JdbcShortAttribute(name, sqlType, length, required,
          null);
      break;
      case Types.TINYINT:
        attribute = new JdbcByteAttribute(name, sqlType, length, required, null);
      break;
      case Types.DOUBLE:
        attribute = new JdbcDoubleAttribute(name, sqlType, length, required,
          null);
      break;
      case Types.REAL:
        attribute = new JdbcFloatAttribute(name, sqlType, length, required,
          null);
      break;
      case Types.DECIMAL:
      case Types.NUMERIC:
      case Types.FLOAT:
        if (scale > 0) {
          attribute = new JdbcBigDecimalAttribute(name, sqlType, length, scale,
            required, null);
        } else if (length == 131089 || length ==0) {
          attribute = new JdbcBigDecimalAttribute(name, sqlType, -1, -1,
            required, null);
        } else {
          if (length <= 2) {
            attribute = new JdbcByteAttribute(name, sqlType, length, required,
              null);
          } else if (length <= 4) {
            attribute = new JdbcShortAttribute(name, sqlType, length, required,
              null);
          } else if (length <= 9) {
            attribute = new JdbcIntegerAttribute(name, sqlType, length,
              required, null);
          } else if (length <= 18) {
            attribute = new JdbcLongAttribute(name, sqlType, length, required,
              null);
          } else {
            attribute = new JdbcBigIntegerAttribute(name, sqlType, length,
              required, null);
          }
        }
      break;
      case Types.DATE:
        attribute = new JdbcDateAttribute(name, sqlType, required, null);
      break;
      case Types.TIMESTAMP:
        attribute = new JdbcTimestampAttribute(name, sqlType, required, null);
      break;
      default:
        attribute = new JdbcAttribute(name, dataType, sqlType, length, scale,
          required, null);
      break;
    }
    metaData.addAttribute(attribute);
    return attribute;
  }

  public void initialize(final DataObjectStoreSchema schema) {
  }
}
