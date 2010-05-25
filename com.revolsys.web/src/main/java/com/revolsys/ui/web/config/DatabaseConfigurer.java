/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.web.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;

import com.revolsys.jdbc.JdbcUtils;

public abstract class DatabaseConfigurer implements BeanFactoryPostProcessor,
  Ordered {
  /** The logger for the instance. */
  private final Logger logger = Logger.getLogger(getClass());

  /**
   * The order value of this object, higher value meaning greater in terms of
   * sorting.
   */
  private int order = Integer.MAX_VALUE;

  /** The data source used to load properties from. */
  private DataSource dataSource;

  /** The name of the table containing configuration properties. */
  private String tableName;

  /** The name of the column containing property keys. */
  private String keyColumnName = "key";

  /** The name of the column containing property values. */
  private String valueColumnName = "value";

  /**
   * @param beanFactory The bean factory the bean is loaded from.
   */
  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory) {
    Map<String,String> properties = new HashMap<String,String>();
    Connection connection  = null;
    try {
       connection = JdbcUtils.getConnection(dataSource);
      String sql = "SELECT " + keyColumnName + ", " + valueColumnName
        + " FROM " + tableName;
      Statement statement = connection.createStatement();
      ResultSet results = statement.executeQuery(sql);
      while (results.next()) {
        String key = results.getString(1);
        String value = results.getString(2);
        properties.put(key, value);
      }

    } catch (SQLException e) {
      throw new BeanInitializationException(e.getMessage(), e);
    } finally {
      JdbcUtils.close(connection);
    }

    processProperties(beanFactory, properties);
  }

  /**
   * <p>
   * Convert the given merged properties, converting property values if
   * necessary. The result will then be processed.
   * </p>
   * <p>
   * Default implementation will invoke <code>convertPropertyValue</code> for
   * each property value, replacing the original with the converted value.
   * </p>
   * 
   * @param properties The properties to convert.
   * @see #convertPropertyValue
   * @see #processProperties
   */
  protected void convertProperties(final Map properties) {
    Iterator propertyNames = properties.keySet().iterator();
    while (propertyNames.hasNext()) {
      String propertyName = (String)propertyNames.next();
      String propertyValue = (String)properties.get(propertyName);
      String convertedValue = convertPropertyValue(propertyValue);
      if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
        properties.put(propertyName, convertedValue);
      }
    }
  }

  /**
   * <p>
   * Convert the given property value from the properties source to the value
   * that should be applied.
   * </p>
   * <p>
   * Default implementation simply returns the original value. Can be overridden
   * in subclasses, for example to detect encrypted values and decrypt them
   * accordingly.
   * </p>
   * 
   * @param originalValue the original value from the properties source
   *          (properties file or local "properties")
   * @return the converted value, to be used for processing
   */
  protected String convertPropertyValue(final String originalValue) {
    return originalValue;
  }

  /**
   * Apply the given Properties to the bean factory.
   * 
   * @param beanFactory the bean factory used by the application context
   * @param properties the Properties to apply
   */
  protected abstract void processProperties(
    ConfigurableListableBeanFactory beanFactory, Map properties);

  /**
   * Set the data source used to load properties from.
   * 
   * @param dataSource The data source used to load properties from.
   */
  public final void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Get the name of the column containing property keys.
   * 
   * @return The name of the column containing property keys.
   */
  public final String getKeyColumnName() {
    return keyColumnName;
  }

  /**
   * Set the name of the column containing property keys.
   * 
   * @param keyColumnName The name of the column containing property keys.
   */
  public final void setKeyColumnName(final String keyColumnName) {
    this.keyColumnName = keyColumnName;
  }

  /**
   * Get the logger.
   * 
   * @return The logger.
   */
  protected Logger getLogger() {
    return logger;
  }

  /**
   * Set the order value of this object, higher value meaning greater in terms
   * of sorting.
   * 
   * @param order The order value of this object, higher value meaning greater
   *          in terms of sorting.
   */
  public void setOrder(final int order) {
    this.order = order;
  }

  /**
   * Get the order value of this object, higher value meaning greater in terms
   * of sorting.
   * 
   * @return The order value of this object, higher value meaning greater in
   *         terms of sorting.
   */
  public int getOrder() {
    return order;
  }

  /**
   * Get the name of the table containing configuration properties.
   * 
   * @return The name of the table containing configuration properties.
   */
  public final String getTableName() {
    return tableName;
  }

  /**
   * Set the name of the table containing configuration properties.
   * 
   * @param tableName The name of the table containing configuration properties.
   */
  public final void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  /**
   * Get The name of the column containing property values.
   * 
   * @return The name of the column containing property values.
   */
  public final String getValueColumnName() {
    return valueColumnName;
  }

  /**
   * Set the name of the column containing property values.
   * 
   * @param valueColumnName The name of the column containing property values.
   */
  public final void setValueColumnName(final String valueColumnName) {
    this.valueColumnName = valueColumnName;
  }

}
