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

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;

import com.revolsys.jdbc.JdbcUtils;

public abstract class DatabaseConfigurer implements BeanFactoryPostProcessor, Ordered {
  /** The LOG for the instance. */
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfigurer.class);

  /** The data source used to load properties from. */
  private DataSource dataSource;

  /** The name of the column containing property keys. */
  private String keyColumnName = "key";

  /**
   * The order value of this object, higher value meaning greater in terms of
   * sorting.
   */
  private int order = Integer.MAX_VALUE;

  /** The name of the table containing configuration properties. */
  private String tableName;

  /** The name of the column containing property values. */
  private String valueColumnName = "value";

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
    final Iterator propertyNames = properties.keySet().iterator();
    while (propertyNames.hasNext()) {
      final String propertyName = (String)propertyNames.next();
      final String propertyValue = (String)properties.get(propertyName);
      final String convertedValue = convertPropertyValue(propertyValue);
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

  @PreDestroy
  public void destroy() {
    this.dataSource = null;
    this.keyColumnName = null;
    this.tableName = null;
    this.valueColumnName = null;
  }

  /**
   * Get the name of the column containing property keys.
   *
   * @return The name of the column containing property keys.
   */
  public final String getKeyColumnName() {
    return this.keyColumnName;
  }

  /**
   * Get the LOG.
   *
   * @return The LOG.
   */
  protected Logger getLog() {
    return LOG;
  }

  /**
   * Get the order value of this object, higher value meaning greater in terms
   * of sorting.
   *
   * @return The order value of this object, higher value meaning greater in
   *         terms of sorting.
   */
  @Override
  public int getOrder() {
    return this.order;
  }

  /**
   * Get the name of the table containing configuration properties.
   *
   * @return The name of the table containing configuration properties.
   */
  public final String getTableName() {
    return this.tableName;
  }

  /**
   * Get The name of the column containing property values.
   *
   * @return The name of the column containing property values.
   */
  public final String getValueColumnName() {
    return this.valueColumnName;
  }

  /**
   * @param beanFactory The bean factory the bean is loaded from.
   */
  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
    final Map<String, String> properties = new HashMap<>();
    Connection connection = null;
    try {
      connection = JdbcUtils.getConnection(this.dataSource);
      final String sql = "SELECT " + this.keyColumnName + ", " + this.valueColumnName + " FROM "
        + this.tableName;
      final Statement statement = connection.createStatement();
      final ResultSet results = statement.executeQuery(sql);
      while (results.next()) {
        final String key = results.getString(1);
        final String value = results.getString(2);
        properties.put(key, value);
      }

    } catch (final SQLException e) {
      throw new BeanInitializationException(e.getMessage(), e);
    } finally {
      JdbcUtils.release(connection, this.dataSource);
    }

    processProperties(beanFactory, properties);
  }

  /**
   * Apply the given Properties to the bean factory.
   *
   * @param beanFactory the bean factory used by the application context
   * @param properties the Properties to apply
   */
  protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory,
    Map properties);

  /**
   * Set the data source used to load properties from.
   *
   * @param dataSource The data source used to load properties from.
   */
  public final void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
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
   * Set the name of the table containing configuration properties.
   *
   * @param tableName The name of the table containing configuration properties.
   */
  public final void setTableName(final String tableName) {
    this.tableName = tableName;
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
