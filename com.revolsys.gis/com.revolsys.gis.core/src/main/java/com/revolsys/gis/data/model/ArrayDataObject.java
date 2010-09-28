/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-core/trunk/src/main/java/com/revolsys/gis/data/model/ArrayDataObject.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2008-10-02 11:14:47 -0700 (Thu, 02 Oct 2008) $
 * $Revision: 1434 $

 * Copyright 2004-2007 Revolution Systems Inc.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The ArrayDataObject is an implementation of {@link DataObject} which uses an
 * array of Objects as the storage for the attribute values.
 * 
 * @author Paul Austin
 */
public class ArrayDataObject extends AbstractMap<String,Object> implements DataObject, Cloneable {
  /** The log instance. */
  private static final Logger LOG = Logger.getLogger(ArrayDataObject.class);

  /** The object's attribute values. */
  private final Object[] attributes;

  /** The metaData defining the object type. */
  private final DataObjectMetaData metaData;

  protected DataObjectState state = DataObjectState.New;

  /**
   * Construct a new ArrayDataObject as a deep clone of the attribute values.
   * Objects can only be cloned if they have a publically accessible
   * {@link #clone()} method.
   * 
   * @param object The object to clone.
   */
  public ArrayDataObject(
    final DataObject object) {
    this(object.getMetaData(), object);
  }

  /**
   * Construct a new empty ArrayDataObject using the metaData.
   * 
   * @param metaData The metaData defining the object type.
   */
  public ArrayDataObject(
    final DataObjectMetaData metaData) {
    this.metaData = metaData;
    attributes = new Object[metaData.getAttributeCount()];
  }

  protected ArrayDataObject(
    final DataObjectMetaData metaData,
    final DataObject object) {
    this.metaData = metaData;
    attributes = new Object[metaData.getAttributeCount()];
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      final Object value = object.getValue(i);
      attributes[i] = clone(value);
    }
  }

  /**
   * Create a clone of the object.
   * 
   * @return The cloned object.
   */
  @Override
  public ArrayDataObject clone() {
    final ArrayDataObject newObject = new ArrayDataObject(this);
    if (metaData.getIdAttributeIndex() != -1) {
      newObject.setIdValue(null);
    }
    return newObject;
  }

  /**
   * Clone the value if it has a clone method.
   * 
   * @param value The value to clone.
   * @return The cloned value.
   */
  private Object clone(
    final Object value) {
    if (value instanceof Cloneable) {
      try {
        final Class<? extends Object> valueClass = value.getClass();
        final Method method = valueClass.getMethod("clone", new Class[0]);
        if (method != null) {
          return method.invoke(value, new Object[0]);
        }
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final InvocationTargetException e) {

        final Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          final RuntimeException re = (RuntimeException)cause;
          throw re;
        } else if (cause instanceof Error) {
          final Error ee = (Error)cause;
          throw ee;
        } else {
          throw new RuntimeException(cause.getMessage(), cause);
        }
      } catch (final RuntimeException e) {
        throw e;
      } catch (final Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }

    }
    return value;
  }

  /**
   * Get the factory which created the instance.
   * 
   * @return The factory.
   */
  public DataObjectFactory getFactory() {
    return ArrayDataObjectFactory.getInstance();
  }

  /**
   * Get the value of the primary geometry attribute.
   * 
   * @return The primary geometry.
   */
  @SuppressWarnings("unchecked")
  public <T extends Geometry> T getGeometryValue() {
    final int index = metaData.getGeometryAttributeIndex();
    return (T)getValue(index);
  }

  /**
   * Get the value of the unique identifier attribute.
   * 
   * @return The unique identifier.
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> T getIdValue() {
    final int index = metaData.getIdAttributeIndex();
    return (T)getValue(index);
  }

  /**
   * Get the metd data describing the DataObject and it's attributes.
   * 
   * @return The meta data.
   */
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public DataObjectState getState() {
    return state;
  }

  /**
   * Get the value of the attribute with the specified name.
   * 
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(
    final CharSequence name) {
    try {
      final int index = metaData.getAttributeIndex(name);
      return (T)getValue(index);
    } catch (final NullPointerException e) {
      LOG.warn("Attribute " + metaData.getName() + "." + name
        + " does not exist");
      return null;
    }
  }

  /**
   * Get the value of the attribute with the specified index.
   * 
   * @param index The index of the attribute.
   * @return The attribute value.
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(
    final int index) {
    if (index < 0) {
      return null;
    } else {
      return (T)attributes[index];
    }
  }

  public Map<String, Object> getValueMap(
    final Collection<? extends CharSequence> attributeNames) {
    final Map<String, Object> values = new HashMap<String, Object>();
    for (final CharSequence name : attributeNames) {
      final Object value = getValue(name);
      if (value != null) {
        values.put(name.toString(), value);
      }
    }
    return values;
  }

  /**
   * Get the values of all attributes.
   * 
   * @return The attribute value.
   */
  public List<Object> getValues() {
    return Arrays.asList(attributes);
  }

  /**
   * Checks to see if the metadata for this DataObject has an attribute with the
   * specified name.
   * 
   * @param name The name of the attribute.
   * @return True if the DataObject has an attribute with the specified name.
   */
  public boolean hasAttribute(
    final CharSequence name) {
    return metaData.hasAttribute(name);
  }

  /**
   * Set the value of the primary geometry attribute.
   * 
   * @param geometry The primary geometry.
   */
  public void setGeometryValue(
    final Geometry geometry) {
    final int index = metaData.getGeometryAttributeIndex();
    setValue(index, geometry);
  }

  /**
   * Set the value of the unique identifier attribute. param id The unique
   * identifier.
   * 
   * @param id The unique identifier.
   */
  public void setIdValue(
    final Object id) {
    if (state != DataObjectState.New) {
      throw new IllegalStateException(
        "Cannot change the ID on a persisted object");
    }
    final int index = metaData.getIdAttributeIndex();
    setValue(index, id);
  }

  public void setState(
    final DataObjectState state) {
    // TODO make this more secure
    this.state = state;
  }

  /**
   * Set the value of the attribute with the specified name.
   * 
   * @param name The name of the attribute.
   * @param value The new value.
   */
  public void setValue(
    final CharSequence name,
    final Object value) {
    final int index = metaData.getAttributeIndex(name);
    if (index >= 0) {
      setValue(index, value);
    } else {

      final int dotIndex = name.toString().indexOf('.');
      if (dotIndex == -1) {

      } else {
        final CharSequence key = name.subSequence(0, dotIndex);
        final CharSequence subKey = name.subSequence(dotIndex + 1,
          name.length());
        Object objectValue = getValue(key);
        if (objectValue == null) {
          final DataType attributeType = metaData.getAttributeType(key);
          if (attributeType != null) {
            if (attributeType.getJavaClass() == DataObject.class) {
              final QName typeName = attributeType.getName();
              final DataObjectMetaDataFactory metaDataFactory = metaData.getDataObjectMetaDataFactory();
              final DataObjectMetaData subMetaData = metaDataFactory.getMetaData(typeName);
              final DataObjectFactory dataObjectFactory = subMetaData.getDataObjectFactory();
              final DataObject subObject = dataObjectFactory.createDataObject(subMetaData);
              subObject.setValue(subKey, value);
              setValue(key, subObject);
            }
          }
        } else {
          if (objectValue instanceof Geometry) {
            Geometry geometry = (Geometry)objectValue;
            JtsGeometryUtil.setGeometryProperty(geometry, subKey, value);
          } else if (objectValue instanceof DataObject) {
            DataObject object = (DataObject)objectValue;
            object.setValue(subKey, value);
          } else {
            JavaBeanUtil.setProperty(objectValue, subKey.toString(), value);
          }
        }
      }
    }
  }

  /**
   * Set the value of the attribute with the specified name.
   * 
   * @param index The index of the attribute. param value The attribute value.
   * @param value The new value.
   */
  public void setValue(
    final int index,
    final Object value) {
    if (index >= 0) {
      updateState();
      attributes[index] = value;
    }
  }

  public void setValues(
    final Map<String, ? extends Object> values) {
    for (final Entry<String, ? extends Object> defaultValue : values.entrySet()) {
      final String name = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      setValue(name, value);
    }
  }

  /**
   * Return a String representation of the Object. There is no guarantee as to
   * the format of this string.
   * 
   * @return The string value.
   */
  @Override
  public String toString() {
    final StringBuffer s = new StringBuffer();
    s.append(metaData.getName()).append("(\n");
    for (int i = 0; i < attributes.length; i++) {
      final Object value = getValue(i);
      if (value != null) {
        s.append(metaData.getAttributeName(i))
          .append('=')
          .append(value)
          .append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  protected void updateState() {
    switch (state) {
      case Persisted:
        state = DataObjectState.Modified;
      break;
      case Deleted:
        throw new IllegalStateException(
          "Cannot modify an object which has been deleted");
    }
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    Set<Entry<String, Object>> entries = new LinkedHashSet<Entry<String,Object>>();
    for(int i = 0; i < attributes.length; i++) {
      entries.add(new DataObjectEntry(this, i));
    }
    return entries;
  }
}
