package com.revolsys.collection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.logging.Logs;
import com.revolsys.spring.config.BeanConfigurrer;
import com.revolsys.util.Property;

public class DatabaseBeanConfigurator extends BeanConfigurrer {

  private DataSource dataSource;

  private String propertyColumnName;

  private String tableName;

  private String typeColumnName;

  private String valueColumnName;

  private String whereClause;

  public DatabaseBeanConfigurator() {
    setOrder(LOWEST_PRECEDENCE - 500);
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public String getPropertyColumnName() {
    return this.propertyColumnName;
  }

  public String getTableName() {
    return this.tableName;
  }

  public String getTypeColumnName() {
    return this.typeColumnName;
  }

  public String getValueColumnName() {
    return this.valueColumnName;
  }

  public String getWhereClause() {
    return this.whereClause;
  }

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    try {
      final boolean hasTypeColumnName = Property.hasValue(this.typeColumnName);
      String sql = "SELECT " + this.propertyColumnName + ", " + this.valueColumnName;
      if (hasTypeColumnName) {
        sql += ", " + this.typeColumnName;
      }
      sql += " FROM " + JdbcUtils.getQualifiedTableName(this.tableName);
      if (Property.hasValue(this.whereClause)) {
        sql += " WHERE " + this.whereClause;
      }
      final Connection connection = this.dataSource.getConnection();
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
              final DataType dataType = DataTypes.getDataType(typePath);
              Object value = valueString;
              if (dataType != null) {
                value = dataType.toObject(valueString);
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
        JdbcUtils.release(connection, this.dataSource);
      }
    } catch (final Throwable e) {
      Logs.error(this, "Unable to load configuration from database ", e);
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
