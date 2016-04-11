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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class DatabasePropertyOverrideConfigurer extends DatabaseConfigurer {
  /**
   * The pattern for setting the value for a key in a Map property of a bean
   * (e.g. bean.property[key]).
   */
  private static final Pattern MAP_PROPERTY_VALUE_PATTERN = Pattern
    .compile("(\\w+)\\.(\\w+)\\[([a-z]\\w*)\\]");

  /**
   * The pattern for setting the value for a property of a bean (e.g.
   * bean.property).
   */
  private static final Pattern SIMPLE_PROPERTY_PATTERN = Pattern.compile("(\\w+)\\.(\\w+)");

  /** Contains names of beans that have overrides */
  private final Set<String> beanNames = Collections.synchronizedSet(new HashSet<String>());

  /**
   * The flag indictaing if invalid keys should be ignored or an exception
   * thrown.
   */
  private boolean ignoreInvalidKeys = false;

  /**
   * Apply the given property value to the corresponding map property on the
   * bean.
   *
   * @param factory The bean factory.
   * @param beanName The name of the bean.
   * @param propertyName The name of the property.
   * @param mapKey The key in the map to set.
   * @param value The value to set.
   */
  protected void applyMapPropertyValue(final ConfigurableListableBeanFactory factory,
    final String beanName, final String propertyName, final String mapKey, final String value) {
    this.beanNames.add(beanName);
    final BeanDefinition bd = factory.getBeanDefinition(beanName);
    final MutablePropertyValues values = bd.getPropertyValues();
    final PropertyValue propertyValue = values.getPropertyValue(propertyName);
    if (propertyValue != null) {
      final Object objectValue = propertyValue.getValue();
      if (objectValue instanceof Map) {
        final Map map = (Map)objectValue;
        map.put(mapKey, value);
      } else {
        throw new BeanInitializationException(
          "Bean property [" + beanName + "." + propertyName + "] is not a Map");
      }
    } else {
      final Map<String, Object> map = new HashMap<>();
      map.put(mapKey, value);
      values.addPropertyValue(propertyName, map);
    }
  }

  /**
   * Apply the given property value to the corresponding bean.
   *
   * @param factory The bean factory.
   * @param beanName The name of the bean.
   * @param propertyName The name of the property.
   * @param value The value to set.
   */
  protected void applyPropertyValue(final ConfigurableListableBeanFactory factory,
    final String beanName, final String propertyName, final String value) {
    this.beanNames.add(beanName);
    final BeanDefinition bd = factory.getBeanDefinition(beanName);
    final MutablePropertyValues values = bd.getPropertyValues();
    final PropertyValue propertyValue = new PropertyValue(propertyName, value);
    propertyValue.setOptional(this.ignoreInvalidKeys);
    values.addPropertyValue(propertyValue);
  }

  /**
   * Were there overrides for this bean? Only valid after processing has
   * occurred at least once.
   *
   * @param beanName name of the bean to query status for
   * @return whether there were property overrides for the named bean
   */
  public boolean hasPropertyOverridesFor(final String beanName) {
    return this.beanNames.contains(beanName);
  }

  /**
   * Process the given key as 'beanName.property' entry.
   *
   * @param factory The bean factory.
   * @param key The key used to set the property.
   * @param value The value to set. @ If there was an problem setting the value.
   */
  protected void processKey(final ConfigurableListableBeanFactory factory, final String key,
    final String value) {
    final Matcher mapProperetyValueMatcher = MAP_PROPERTY_VALUE_PATTERN.matcher(key);
    if (mapProperetyValueMatcher.matches()) {
      final String beanName = mapProperetyValueMatcher.group(1);
      final String beanProperty = mapProperetyValueMatcher.group(2);
      final String mapKey = mapProperetyValueMatcher.group(3);
      applyMapPropertyValue(factory, beanName, beanProperty, mapKey, value);
    } else {
      final Matcher simpleMatcher = SIMPLE_PROPERTY_PATTERN.matcher(key);
      if (simpleMatcher.matches()) {
        final String beanName = simpleMatcher.group(1);
        final String beanProperty = simpleMatcher.group(2);
        applyPropertyValue(factory, beanName, beanProperty, value);
      } else {
        throw new BeanInitializationException("Invalid key [" + key + "]");
      }

    }
    if (getLog().isDebugEnabled()) {
      getLog().debug("WebProperty '" + key + "' set to [" + value + "]");
    }
  }

  /**
   * @param beanFactory The bean factory.
   * @param properties The property name and values to set. @ If there was an
   *          problem setting the values.
   */
  @Override
  protected void processProperties(final ConfigurableListableBeanFactory beanFactory,
    final Map properties) {
    for (final Iterator en = properties.keySet().iterator(); en.hasNext();) {
      final String key = (String)en.next();
      try {
        processKey(beanFactory, key, (String)properties.get(key));
      } catch (final BeansException ex) {
        final String msg = "Could not process key [" + key + "] in PropertyOverrideConfigurer";
        if (this.ignoreInvalidKeys) {
          if (getLog().isDebugEnabled()) {
            getLog().debug(msg, ex);
          } else {
            getLog().warn(msg + ": " + ex.getMessage());
          }
        } else {
          throw new BeanInitializationException(msg, ex);
        }
      }
    }
  }

  /**
   * <p>
   * Set the flag indictaing if invalid keys should be ignored or an exception
   * thrown. If you ignore invalid keys, keys that do not follow the
   * 'beanName.property' format will just be logged as warning. This allows to
   * have arbitrary other keys in a properties file.
   * </p>
   *
   * @param ignore The flag indictaing if invalid keys should be ignored or an
   *          exception thrown.
   */
  public void setIgnoreInvalidKeys(final boolean ignore) {
    this.ignoreInvalidKeys = ignore;
  }

}
