package com.revolsys.gis.format.mdb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class MdbSchemaUtil {
  // TODO convert to data store
  private final Map<QName, DataObjectMetaDataImpl> typeMetaData = new HashMap<QName, DataObjectMetaDataImpl>();

  public MdbSchemaUtil(
    final Connection connection) {
    try {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();
      final ResultSet tableMetaData = databaseMetaData.getTables(null, null,
        null, new String[] {
          "TABLE"
        });
      while (tableMetaData.next()) {
        final String tableName = tableMetaData.getString("TABLE_NAME");
        final QName typeName = new QName(tableName);
        final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
          typeName);
        typeMetaData.put(typeName, metaData);
        final String sql = "select * from " + tableName;

        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(sql);
        final ResultSetMetaData attributeMetaData = resultSet.getMetaData();
        for (int colNum = 1; colNum < attributeMetaData.getColumnCount(); colNum++) {
          final String attrName = attributeMetaData.getColumnName(colNum);
          final int len = attributeMetaData.getPrecision(colNum);

          DataType dataType;
          switch (attributeMetaData.getColumnType(colNum)) {
            case Types.BOOLEAN:
              dataType = DataTypes.BOOLEAN;
            break;
            case Types.CHAR:
              dataType = DataTypes.STRING;
            break;
            case Types.DATE:
              dataType = DataTypes.DATE_TIME;
            break;
            case Types.DOUBLE:
              dataType = DataTypes.DOUBLE;
            break;
            case Types.DECIMAL:
              dataType = DataTypes.DECIMAL;
            break;
            case Types.FLOAT:
              dataType = DataTypes.FLOAT;
            break;
            case Types.INTEGER:
              dataType = DataTypes.INTEGER;
            break;
            case Types.NUMERIC:
              dataType = DataTypes.DECIMAL;
            break;
            case Types.TIMESTAMP:
              dataType = DataTypes.DATE_TIME;
            break;
            case Types.VARCHAR:
              dataType = DataTypes.STRING;
            break;
            default:
              dataType = DataTypes.OBJECT;
            break;
          }
          metaData.addAttribute(attrName, dataType, len, false);
        }
      }
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public List<String> getColumnNames(
    final QName typeName) {
    return getMetaData(typeName).getAttributeNames();
  }

  public DataObjectMetaData getMetaData(
    final QName typeName) {
    return typeMetaData.get(typeName);
  }

  public DataObjectMetaData getMetaData(
    final QName typeName,
    final ResultSetMetaData metadata) {
    return null;
  }

  public String getPrimaryKeyColumnName(
    final QName typeName) {
    return null;
  }
}
