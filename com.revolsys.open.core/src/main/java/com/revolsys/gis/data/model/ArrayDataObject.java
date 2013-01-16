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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The ArrayDataObject is an implementation of {@link DataObject} which uses an
 * array of Objects as the storage for the attribute values.
 * 
 * @author Paul Austin
 */
public class ArrayDataObject extends BaseDataObject {
  /** Serialization version */
  private static final long serialVersionUID = 2704226494490082708L;

  /** The object's attribute values. */
  private final Object[] attributes;

  /**
   * Construct a new ArrayDataObject as a deep clone of the attribute values.
   * Objects can only be cloned if they have a publically accessible
   * {@link #cloneCoordinates()} method.
   * 
   * @param object The object to clone.
   */
  public ArrayDataObject(final DataObject object) {
    this(object.getMetaData(), object);
  }

  /**
   * Construct a new empty ArrayDataObject using the metaData.
   * 
   * @param metaData The metaData defining the object type.
   */
  public ArrayDataObject(final DataObjectMetaData metaData) {
    super(metaData);
    final int attributeCount = metaData.getAttributeCount();
    attributes = new Object[attributeCount];
  }

  public ArrayDataObject(final DataObjectMetaData metaData,
    final DataObject object) {
    this(metaData);
    setValues(object);
  }

  /**
   * Create a clone of the object.
   * 
   * @return The cloned object.
   */
  @Override
  public ArrayDataObject clone() {
    return (ArrayDataObject)super.clone();
  }

  /**
   * Get the value of the attribute with the specified index.
   * 
   * @param index The index of the attribute.
   * @return The attribute value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final int index) {
    if (index < 0) {
      return null;
    } else {
      return (T)attributes[index];
    }
  }

  /**
   * Get the values of all attributes.
   * 
   * @return The attribute value.
   */
  @Override
  public List<Object> getValues() {
    return Arrays.asList(attributes);
  }

  @Override
  public int hashCode() {
    return attributes.hashCode();
  }

  /**
   * Set the value of the attribute with the specified name.
   * 
   * @param index The index of the attribute. param value The attribute value.
   * @param value The new value.
   */
  @Override
  public void setValue(final int index, final Object value) {
    if (index >= 0) {
      final Object oldValue = attributes[index];
      if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
        updateState();
        attributes[index] = value;
      }
    }
  }
}
