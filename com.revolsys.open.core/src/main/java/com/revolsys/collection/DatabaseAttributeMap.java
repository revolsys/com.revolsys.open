package com.revolsys.collection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.JdbcUtils;

public class DatabaseAttributeMap extends AttributeMap {

  private DataSource dataSource;

  private QName tableName;

  private String propertyColumnName;

  private String valueColumnName;

  private String typeColumnName;

  private String whereClause;

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public QName getTableName() {
    return tableName;
  }

  public void setTableName(QName tableName) {
    this.tableName = tableName;
  }

  public String getPropertyColumnName() {
    return propertyColumnName;
  }

  public void setPropertyColumnName(String propertyColumnName) {
    this.propertyColumnName = propertyColumnName;
  }

  public String getValueColumnName() {
    return valueColumnName;
  }

  public void setValueColumnName(String valueColumnName) {
    this.valueColumnName = valueColumnName;
  }

  public String getTypeColumnName() {
    return typeColumnName;
  }

  public void setTypeColumnName(String typeColumnName) {
    this.typeColumnName = typeColumnName;
  }

  public String getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
  }

  @PostConstruct
  public void initialize() throws Exception {
    final boolean hasTypeColumnName = StringUtils.hasText(typeColumnName);
    String sql = "SELECT " + propertyColumnName + ", " + valueColumnName;
    if (hasTypeColumnName) {
      sql += ", " + typeColumnName;
    }
    sql += " FROM " + JdbcUtils.getTableName(tableName);
    if (StringUtils.hasText(whereClause)) {
      sql += " WHERE " + whereClause;
    }
    final Connection connection = dataSource.getConnection();
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        final ResultSet resultSet = statement.executeQuery();
        try {
          Map<String, Object> attributes = new LinkedHashMap<String, Object>();
          while (resultSet.next()) {
            final String property = resultSet.getString(1);
            final String valueString = resultSet.getString(2);
            String typeName = "string";
            if (hasTypeColumnName) {
              typeName = resultSet.getString(3);
            }
            final DataType dataType = DataTypes.getType(QName.valueOf(typeName));
            Object value = valueString;
            if (dataType != null) {
              final Class<?> dataTypeClass = dataType.getJavaClass();
              final StringConverter<?> converter = StringConverterRegistry.INSTANCE.getConverter(dataTypeClass);
              if (converter != null) {
                value = converter.toObject(valueString);
              }
            }
            attributes.put(property, value);
          }

        } finally {
          JdbcUtils.close(resultSet);
        }
      } finally {
        JdbcUtils.close(statement);
      }
    } finally {
      JdbcUtils.close(connection);
    }
  }
}
