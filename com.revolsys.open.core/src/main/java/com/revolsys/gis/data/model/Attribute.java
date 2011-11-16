/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-core/trunk/src/main/java/com/revolsys/gis/data/model/metadata/Attribute.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2008-05-29 08:12:08 -0700 (Thu, 29 May 2008) $
 * $Revision: 1307 $
 *
 * Copyright 2004-2008 Revolution Systems Inc.
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
package com.revolsys.gis.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;

/**
 * The Attribute class defines the name, type and other properties about each
 * attribute on a {@link DataObject} in the {@link DataObjectMetaData}.
 * 
 * @author Paul Austin
 * @see DataObject
 * @see DataObjectMetaData
 */
public class Attribute implements Cloneable {
  /** The description of the attribute. */
  private String description;

  private int index;

  /** The maximum length of an attribute value. */
  private int length;

  /** The name of the attribute. */
  private String name;

  /** The meta data properties about the attribute. */
  private final Map<QName, Object> properties = new HashMap<QName, Object>();

  /** The flag indicating if a value is required for the attribute. */
  private boolean required;

  /** The maximum number of decimal places. */
  private int scale;

  /** The data type of the attribute value. */
  private DataType type;

  private List<Object> allowedValues = new ArrayList<Object>();

  public Attribute() {
  }

  public Attribute(final Attribute attribute) {
    this.name = attribute.getName();
    this.description = attribute.getDescription();
    this.type = attribute.getType();
    this.required = attribute.isRequired();
    this.length = attribute.getLength();
    this.scale = attribute.getScale();
    final Map<QName, Object> properties = attribute.getProperties();
    if (properties != null) {
      this.properties.putAll(properties);
    }

  }

  public Attribute(final int index) {
    this.index = index;
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   */
  public Attribute(final String name, final DataType type,
    final boolean required) {
    this(name, type, 0, 0, required, null, null);
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type,
    final boolean required, final Map<QName, Object> properties) {
    this(name, type, 0, 0, required, properties);
  }

  public Attribute(final String name, final DataType dataType,
    final boolean required, final String description) {
    this(name, dataType, 0, 0, required, description, null);
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   */
  public Attribute(final String name, final DataType type, final int length,
    final boolean required) {
    this(name, type, length, 0, required, null, null);
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type, final int length,
    final boolean required, final Map<QName, Object> properties) {
    this(name, type, length, 0, required, properties);
  }

  public Attribute(final String name, final DataType type, final int length,
    final boolean required, final String description) {
    this(name, type, length, 0, required, description, null);
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type,
    final Integer length, final Integer scale, final Boolean required) {
    this(name, type, length, scale, required, "");
  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type,
    final Integer length, final Integer scale, final Boolean required,
    final Map<QName, Object> properties) {
    this(name, type, length, scale, required, null, properties);

  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type,
    final Integer length, final Integer scale, final Boolean required,
    final String description, final Map<QName, Object> properties) {
    this.name = name;
    this.description = description;
    this.type = type;
    if (required != null) {
      this.required = required;
    }
    if (length != null) {
      this.length = length;
    }
    if (scale != null) {
      this.scale = scale;
    }
    this.description = description;
    if (properties != null) {
      this.properties.putAll(properties);
    }

  }

  /**
   * Construct a new attribute.
   * 
   * @param name The name of the attribute.
   * @param type The data type of the attribute value.
   * @param length The maximum length of an attribute value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          attribute.
   * @param properties The meta data properties about the attribute.
   */
  public Attribute(final String name, final DataType type,
    final Integer length, final Integer scale, final Boolean required,
    final String description) {
    this.name = name;
    this.description = description;
    this.type = type;
    if (required != null) {
      this.required = required;
    }
    if (length != null) {
      this.length = length;
    }
    if (scale != null) {
      this.scale = scale;
    }
    this.description = description;
  }

  @Override
  public Attribute clone() {
    return new Attribute(this);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Attribute) {
      final Attribute attribute = (Attribute)object;
      return (name.equals(attribute.getName()));
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getAllowedValues() {
    return (List<T>)allowedValues;
  }

  public String getDescription() {
    return description;
  }

  public int getIndex() {
    return index;
  }

  /**
   * Get the maximum length of the attribute value. The length 0 should be used
   * if there is no maximum.
   * 
   * @return The maximum length of an attribute value.
   */
  public int getLength() {
    return length;
  }

  /**
   * Get the name of the attribute.
   * 
   * @return The name of the attribute.
   */
  public String getName() {
    return name;
  }

  /**
   * Get all meta data properties about the attribute.
   * 
   * @return The properties.
   */
  public Map<QName, Object> getProperties() {
    return properties;
  }

  /**
   * Get the named meta data property about the attribute.
   * 
   * @param <T> The type to cast the value to.
   * @param name The name of the property.
   * @return The property value.
   */
  @SuppressWarnings("unchecked")
  public <V> V getProperty(final QName name) {
    return (V)properties.get(name);
  }

  /**
   * Get the maximum number of decimal places of the attribute value.
   * 
   * @return The maximum number of decimal places.
   */
  public int getScale() {
    return scale;
  }

  /**
   * Get the data type of the attribute value.
   * 
   * @return The data type of the attribute value.
   */
  public DataType getType() {
    return type;
  }

  /**
   * Return the hash code of the attribute.
   * 
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Get the flag indicating if a value is required for the attribute.
   * 
   * @return True if a value is required, false otherwise.
   */
  public boolean isRequired() {
    return required;
  }

  public void setAllowedValues(final List<?> allowedValues) {
    this.allowedValues.addAll(allowedValues);
  }

  void setIndex(final int index) {
    this.index = index;
  }

  public void setProperty(final QName name, final Object value) {
    properties.put(name, value);
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer(name);
    string.append(':');
    string.append(type);
    if (length > 0) {
      string.append('(');
      string.append(length);
      if (scale > 0) {
        string.append(',');
        string.append(scale);
      }
      string.append(')');
    }
    return string.toString();
  }
}
