package com.revolsys.collection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.spring.config.BeanConfigurrer;

public class DatabaseBeanConfigurator extends BeanConfigurrer {

  private DataSource dataSource;

  private String tableName;

  private String propertyColumnName;

  private String valueColumnName;

  private String typeColumnName;

  private String whereClause;

  public DatabaseBeanConfigurator() {
    setOrder(LOWEST_PRECEDENCE - 500);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public String getPropertyColumnName() {
    return propertyColumnName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getTypeColumnName() {
    return typeColumnName;
  }

  public String getValueColumnName() {
    return valueColumnName;
  }

  public String getWhereClause() {
    return whereClause;
  }

  @Override
  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory) throws BeansException {
    try {
      final boolean hasTypeColumnName = StringUtils.hasText(typeColumnName);
      String sql = "SELECT " + propertyColumnName + ", " + valueColumnName;
      if (hasTypeColumnName) {
        sql += ", " + typeColumnName;
      }
      sql += " FROM " + JdbcUtils.getQualifiedTableName(tableName);
      if (StringUtils.hasText(whereClause)) {
        sql += " WHERE " + whereClause;
      }
      final Connection connection = dataSource.getConnection();
      try {
        final PreparedStatement statement = connection.prepareStatement(sql);
        try {
          final ResultSet resultSet = statement.executeQuery();
          try {
            while (resultSet.next()) {
              final String property = resultSet.getString(1);
              final String valueString = resultSet.getString(2);
              String typePath = "string";
              if (hasTypeColumnName) {
                typePath = resultSet.getString(3);
              }
              final DataType dataType = DataTypes.getType(typePath);
              Object value = valueString;
              if (dataType != null) {
                final Class<?> dataTypeClass = dataType.getJavaClass();
                final StringConverter<?> converter = StringConverterRegistry.getInstance()
                  .getConverter(dataTypeClass);
                if (converter != null) {
                  value = converter.toObject(valueString);
                }
              }
              setAttribute(property, value);
            }

          } finally {
            JdbcUtils.close(resultSet);
          }
        } finally {
          JdbcUtils.close(statement);
        }
      } finally {
        JdbcUtils.release(connection, dataSource);
      }
    } catch (final Throwable e) {
      LOG.error("Unable to load configuration from database ", e);
    } finally {
      super.postProcessBeanFactory(beanFactory);
    }
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setPropertyColumnName(final String propertyColumnName) {
    this.propertyColumnName = propertyColumnName;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  public void setTypeColumnName(final String typeColumnName) {
    this.typeColumnName = typeColumnName;
  }

  public void setValueColumnName(final String valueColumnName) {
    this.valueColumnName = valueColumnName;
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }
}
