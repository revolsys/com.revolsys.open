/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/java/com/revolsys/gis/processor/AddDefaultValuesProcess.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-04-29 00:28:10Z $
 * $Revision: 112 $

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
package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.data.types.DataType;
import com.revolsys.io.PathUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class AddDefaultValuesProcess extends
  AbstractInOutProcess<Record, Record> {
  private static final Logger log = Logger.getLogger(AddDefaultValuesProcess.class);

  private Set<String> excludedAttributeNames = new HashSet<String>();

  private RecordDefinitionFactory metaDataFactory;

  private String schemaName;

  private final Map<RecordDefinition, Map<String, Object>> typeDefaultValues = new HashMap<RecordDefinition, Map<String, Object>>();

  private void addDefaultValues(final Map<String, Object> defaultValues,
    final RecordDefinition type) {
    if (PathUtil.getPath(type.getPath()).equals(schemaName)) {
      defaultValues.putAll(type.getDefaultValues());
    }
  }

  private Map<String, Object> getDefaultValues(final RecordDefinition type) {
    if (schemaName == null) {
      return type.getDefaultValues();
    } else {
      Map<String, Object> defaultValues = typeDefaultValues.get(type);
      if (defaultValues == null) {
        defaultValues = new HashMap<String, Object>();
        addDefaultValues(defaultValues, type);
        typeDefaultValues.put(type, defaultValues);
      }
      return defaultValues;
    }
  }

  /**
   * Get the list of attribute names that will be excluded from having the
   * default values set.
   * 
   * @return The names of the attributes to exclude.
   */
  public Set<String> getExcludedAttributeNames() {
    return excludedAttributeNames;
  }

  public RecordDefinitionFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  /**
   * Get the schema name of the type definitions to get the default values from.
   * 
   * @return The schema name.
   */
  public String getSchemaName() {
    return schemaName;
  }

  private void process(final Record dataObject) {
    final RecordDefinition type = dataObject.getMetaData();

    boolean process = true;
    if (schemaName != null) {
      if (!PathUtil.getPath(type.getPath()).equals(schemaName)) {
        process = false;
      }
    }
    if (process) {
      processDefaultValues(dataObject, getDefaultValues(type));
    }

    for (int i = 0; i < type.getAttributeCount(); i++) {
      final Object value = dataObject.getValue(i);
      if (value instanceof Record) {
        process((Record)value);
      }
    }
  }

  private void processDefaultValues(final Record dataObject,
    final Map<String, Object> defaultValues) {
    for (final Iterator<Entry<String, Object>> defaults = defaultValues.entrySet()
      .iterator(); defaults.hasNext();) {
      final Entry<String, Object> defaultValue = defaults.next();
      final String key = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      setDefaultValue(dataObject, key, value);
    }
  }

  @Override
  protected void run(final Channel<Record> in, final Channel<Record> out) {
    for (Record dataObject = in.read(); dataObject != null; dataObject = in.read()) {
      process(dataObject);
      out.write(dataObject);
    }
  }

  private void setDefaultValue(final Record dataObject, final String key,
    final Object value) {
    final int dotIndex = key.indexOf('.');
    if (dotIndex == -1) {
      if (dataObject.getValue(key) == null
        && !excludedAttributeNames.contains(key)) {
        log.info("Adding attribute " + key + "=" + value);
        dataObject.setValue(key, value);
      }
    } else {
      final String attributeName = key.substring(0, dotIndex);
      NDC.push(" -> " + attributeName);
      try {
        final String subKey = key.substring(dotIndex + 1);
        final Object attributeValue = dataObject.getValue(attributeName);
        if (attributeValue == null) {
          final RecordDefinition type = dataObject.getMetaData();
          final int attrIndex = type.getAttributeIndex(attributeName);
          final DataType dataType = type.getAttributeType(attrIndex);
          final Class<?> typeClass = dataType.getJavaClass();
          if (typeClass == Record.class) {

            final RecordDefinition subClass = metaDataFactory.getRecordDefinition(dataType.getName());
            final Record subObject = subClass.createRecord();
            setDefaultValue(subObject, subKey, value);
            dataObject.setValue(attributeName, subObject);
            process(subObject);
          }
        } else if (attributeValue instanceof Record) {
          final Record subObject = (Record)attributeValue;
          setDefaultValue(subObject, subKey, value);
        } else if (!attributeName.equals(dataObject.getMetaData()
          .getGeometryAttributeName())) {
          log.error("Attribute '" + attributeName + "' must be a Record");
        }
      } finally {
        NDC.pop();
      }
    }
  }

  /**
   * Set the list of attribute names that will be excluded from having the
   * default values set.
   * 
   * @param excludedAttributeNames The names of the attributes to exclude.
   */
  public void setExcludedAttributeNames(final Set<String> excludedAttributeNames) {
    this.excludedAttributeNames = excludedAttributeNames;
  }

  public void setMetaDataFactory(final RecordDefinitionFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }

  /**
   * Set the schema name of the type definitions to get the default values from.
   * 
   * @param schemaName The schema name.
   */
  public void setSchemaName(final String schemaName) {
    this.schemaName = schemaName;
  }

}
