/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-core/trunk/src/main/java/com/revolsys/gis/model/schema/impl/SimpleSchemaDefinition.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2008-02-15 10:24:38 -0800 (Fri, 15 Feb 2008) $
 * $Revision: 991 $

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
package com.revolsys.gis.data.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public class DataObjectMetaDataFactoryImpl implements DataObjectMetaDataFactory {
  /** The meta data properties of the data type. */
  private final Map<String, Object> properties = new HashMap<String, Object>();

  private final Map<QName, DataObjectMetaData> types = new LinkedHashMap<QName, DataObjectMetaData>();

  public void addMetaData(final DataObjectMetaData type) {
    if (type != null) {
      types.put(type.getName(), type);
    }
  }

  public DataObjectMetaData getMetaData(final QName typeName) {
    return types.get(typeName);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty(final String name) {
    return (V)properties.get(name);
  }

  public void setProperty(final String name, final Object value) {
    properties.put(name, value);
  }

}
