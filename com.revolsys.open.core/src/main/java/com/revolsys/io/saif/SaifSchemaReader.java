/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/saif/io/SaifSchemaReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

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
package com.revolsys.io.saif;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataFactoryImpl;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.types.CollectionDataType;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.model.types.EnumerationDataType;
import com.revolsys.gis.data.model.types.SimpleDataType;
import com.revolsys.io.saif.util.CsnIterator;

public class SaifSchemaReader {

  private static final Map<QName, DataType> nameTypeMap = new HashMap<QName, DataType>();

  private static final QName SPATIAL_OBJECT = new QName("SpatialObject");

  private static final QName TEXT_OR_SYMBOL_OBJECT = new QName(
    "TextOrSymbolObject");

  static {
    addType("Boolean", DataTypes.BOOLEAN);
    addType("Numeric", DataTypes.DECIMAL);
    addType("Integer", DataTypes.INTEGER);
    addType("Integer8", DataTypes.BYTE);
    addType("Integer16", DataTypes.SHORT);
    addType("Integer32", DataTypes.INT);
    addType("Integer64", DataTypes.LONG);
    addType("Integer8Unsigned", DataTypes.INTEGER);
    addType("Integer16Unsigned", DataTypes.INTEGER);
    addType("Integer32Unsigned", DataTypes.INTEGER);
    addType("Integer64Unsigned", DataTypes.INTEGER);
    addType("Real", DataTypes.DECIMAL);
    addType("Real32", DataTypes.FLOAT);
    addType("Real64", DataTypes.DOUBLE);
    addType("Real80", DataTypes.DECIMAL);
    addType("List", DataTypes.LIST);
    addType("Set", DataTypes.SET);
    addType("AggregateType", new SimpleDataType("AggregateType", Object.class));
    addType("PrimitiveType", new SimpleDataType("PrimitiveType", Object.class));
    addType("Enumeration", new SimpleDataType("Enumeration", Object.class));

  }

  private static void addType(final String typeName, final DataType dataType) {
    nameTypeMap.put(QName.valueOf(typeName), dataType);
  }

  private List<DataObjectMetaDataProperty> commonMetaDataProperties = new ArrayList<DataObjectMetaDataProperty>();

  private DataObjectMetaDataImpl currentClass;

  private final Set<DataObjectMetaData> currentSuperClasses = new LinkedHashSet<DataObjectMetaData>();

  private DataObjectMetaDataFactoryImpl schema;

  private void addExportedObjects() {
    final DataObjectMetaDataImpl exportedObjectHandle = new DataObjectMetaDataImpl(
      new QName("ExportedObjectHandle"));
    schema.addMetaData(exportedObjectHandle);
    exportedObjectHandle.addAttribute("referenceID", DataTypes.STRING, true);
    exportedObjectHandle.addAttribute("type", DataTypes.STRING, true);
    exportedObjectHandle.addAttribute("offset", DataTypes.INTEGER, true);
    exportedObjectHandle.addAttribute("sharable", DataTypes.BOOLEAN, true);
  }

  public void addSuperClass(
    final DataObjectMetaDataImpl currentClass,
    final DataObjectMetaData superClass) {
    currentClass.addSuperClass(superClass);
    for (final String name : superClass.getAttributeNames()) {
      final Attribute attribute = superClass.getAttribute(name);
      currentClass.addAttribute(attribute.clone());
    }
    for (final Entry<String, Object> defaultValue : superClass.getDefaultValues()
      .entrySet()) {
      final String name = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      if (!currentClass.hasAttribute(name)) {
        currentClass.addDefaultValue(name, value);
      }
    }
    final String idAttributeName = superClass.getIdAttributeName();
    if (idAttributeName != null) {
      currentClass.setIdAttributeName(idAttributeName);

    }
    final String geometryAttributeName = superClass.getGeometryAttributeName();
    if (geometryAttributeName != null) {
      currentClass.setGeometryAttributeName(geometryAttributeName);
    }
  }

  public void attributes(
    final DataObjectMetaData type,
    final CsnIterator iterator) throws IOException {
    while (iterator.getNextEventType() == CsnIterator.ATTRIBUTE_NAME
      || iterator.getNextEventType() == CsnIterator.OPTIONAL_ATTRIBUTE) {
      boolean required = true;
      switch (iterator.next()) {
        case CsnIterator.OPTIONAL_ATTRIBUTE:
          required = false;
          iterator.next();
        case CsnIterator.ATTRIBUTE_NAME:
          final String attributeName = iterator.getStringValue();
          switch (iterator.next()) {
            case CsnIterator.ATTRIBUTE_TYPE:
              final QName typeName = iterator.getQNameValue();
              DataType dataType = nameTypeMap.get(typeName);
              if (typeName.equals(SPATIAL_OBJECT)
                || typeName.equals(TEXT_OR_SYMBOL_OBJECT)) {
                dataType = DataTypes.GEOMETRY;
                currentClass.setGeometryAttributeIndex(currentClass.getAttributeCount());
              } else if (dataType == null) {
                dataType = new SimpleDataType(typeName, DataObject.class);
              }

              currentClass.addAttribute(attributeName, dataType, required);

            break;
            case CsnIterator.COLLECTION_ATTRIBUTE:
              final QName collectionType = iterator.getQNameValue();
              if (iterator.next() == CsnIterator.CLASS_NAME) {
                final QName contentTypeName = iterator.getQNameValue();
                final DataType collectionDataType = nameTypeMap.get(collectionType);
                DataType contentDataType = nameTypeMap.get(contentTypeName);
                if (contentDataType == null) {
                  contentDataType = DataTypes.DATA_OBJECT;
                }
                currentClass.addAttribute(attributeName,
                  new CollectionDataType(collectionDataType.getName(),
                    collectionDataType.getJavaClass(), contentDataType),
                  required);
              } else {
                throw new IllegalStateException("Expecting attribute type");
              }
            break;
            case CsnIterator.STRING_ATTRIBUTE:
              int length = Integer.MAX_VALUE;
              if (iterator.getEventType() == CsnIterator.STRING_ATTRIBUTE_LENGTH) {
                length = iterator.getIntegerValue();
              }
              currentClass.addAttribute(attributeName, DataTypes.STRING,
                length, required);
            break;
            default:
              throw new IllegalStateException("Unknown event type: "
                + iterator.getEventType());
          }
        break;
        default:
        break;
      }
    }
  }

  public void classAttributes(
    final DataObjectMetaData type,
    final CsnIterator iterator) throws IOException {
  }

  public void comments(final DataObjectMetaData type, final CsnIterator iterator)
    throws IOException {
    if (iterator.next() == CsnIterator.VALUE) {
      iterator.getStringValue();
    }
  }

  public void defaults(final DataObjectMetaData type, final CsnIterator iterator)
    throws IOException {
    while (iterator.getNextEventType() == CsnIterator.ATTRIBUTE_PATH) {
      iterator.next();
      final String attributeName = iterator.getStringValue();
      if (iterator.next() == CsnIterator.VALUE) {
        final Object value = iterator.getValue();
        currentClass.addDefaultValue(attributeName, value);
      } else {
        throw new IllegalStateException("Expecting a value");
      }
    }
  }

  public List<DataObjectMetaDataProperty> getCommonMetaDataProperties() {
    return commonMetaDataProperties;
  }

  private Object getDefinition(final CsnIterator iterator) throws IOException {
    while (iterator.next() != CsnIterator.END_DEFINITION) {
      switch (iterator.getEventType()) {
        case CsnIterator.CLASS_NAME:
          final QName superClassName = iterator.getQNameValue();
          if (superClassName.equals(new QName("Enumeration"))) {
            final DataType enumeration = processEnumeration(iterator);
            nameTypeMap.put(enumeration.getName(), enumeration);
            return enumeration;
          }
          final DataObjectMetaData superClass = schema.getMetaData(superClassName);
          if (superClass == null) {
            throw new IllegalStateException("Cannot find super class '"
              + superClassName + "'");

          }
          currentSuperClasses.add(superClass);
        break;
        case CsnIterator.COMPONENT_NAME:
          final String componentName = iterator.getStringValue();
          try {
            final Method method = getClass().getMethod(componentName,
              new Class[] {
                DataObjectMetaData.class, CsnIterator.class
              });
            method.invoke(this, new Object[] {
              currentClass, iterator
            });
          } catch (final SecurityException e) {
            throw new IllegalStateException("Unknown component '"
              + componentName + "'");
          } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Unknown component '"
              + componentName + "'");
          } catch (final IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
          } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
              throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
              throw (Error)cause;
            } else if (cause instanceof IOException) {
              throw (IOException)cause;
            } else {
              throw new RuntimeException(cause.getMessage(), cause);
            }
          }
        default:
        break;
      }
    }
    return currentClass;
  }

  private DataObjectMetaDataFactory loadSchema(final CsnIterator iterator)
    throws IOException {
    if (schema == null) {
      schema = new DataObjectMetaDataFactoryImpl();

      schema.addMetaData(new DataObjectMetaDataImpl(new QName("AggregateType")));
      schema.addMetaData(new DataObjectMetaDataImpl(new QName("PrimitiveType")));

      addExportedObjects();
    }
    while (iterator.next() != CsnIterator.END_DOCUMENT) {
      currentSuperClasses.clear();
      currentClass = null;
      final Object definition = getDefinition(iterator);
      if (definition instanceof DataObjectMetaData) {
        final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)definition;
        setMetaDataProperties(metaData);
        metaData.setDataObjectMetaDataFactory(schema);
        schema.addMetaData(metaData);
      }
    }
    return schema;
  }

  public DataObjectMetaDataFactory loadSchema(final File file)
    throws IOException {
    final CsnIterator iterator = new CsnIterator(file);
    return loadSchema(iterator);
  }

  public DataObjectMetaDataFactory loadSchema(final String resource)
    throws IOException {
    final InputStream in = getClass().getResourceAsStream("/" + resource);
    if (in == null) {
      throw new FileNotFoundException(resource);
    }
    return loadSchema(new CsnIterator(resource, in));

  }

  public DataObjectMetaDataFactory loadSchema(
    final String fileName,
    final InputStream in) throws IOException {
    return loadSchema(new CsnIterator(fileName, in));
  }

  public DataObjectMetaDataFactory loadSchemas(final List<String> fileNames)
    throws IOException {
    for (final String fileName : fileNames) {
      final File file = new File(fileName);
      if (file.exists()) {
        loadSchema(file);
      } else {
        loadSchema(fileName);
      }
    }
    return schema;
  }

  private DataType processEnumeration(final CsnIterator iterator)
    throws IOException {
    QName name = null;
    final Set<String> allowedValues = new TreeSet<String>();
    while (iterator.getNextEventType() == CsnIterator.COMPONENT_NAME) {
      iterator.next();
      final String componentName = iterator.getStringValue();
      if (componentName.equals("subclass")) {
        if (iterator.next() == CsnIterator.CLASS_NAME) {
          name = iterator.getQNameValue();
        } else {
          throw new IllegalArgumentException(
            "Expecting an enumeration class name");
        }
      } else if (componentName.equals("values")) {
        while (iterator.getNextEventType() == CsnIterator.TAG_NAME) {
          iterator.next();
          final String tagName = iterator.getStringValue();
          allowedValues.add(tagName);
        }
      } else if (!componentName.equals("comments")) {
        throw new IllegalArgumentException("Unknown component " + componentName
          + " for enumberation " + name);
      }

    }
    return new EnumerationDataType(name, String.class, allowedValues);
  }

  public void restricted(
    final DataObjectMetaData type,
    final CsnIterator iterator) throws IOException {
    while (iterator.getNextEventType() == CsnIterator.ATTRIBUTE_PATH) {
      iterator.next();
      String attributeName = iterator.getStringValue();
      boolean hasMore = true;
      final List<QName> typeNames = new ArrayList<QName>();
      final List<Object> values = new ArrayList<Object>();
      while (hasMore) {
        switch (iterator.getNextEventType()) {
          case CsnIterator.CLASS_NAME:
            iterator.next();
            final QName typeName = iterator.getQNameValue();
            typeNames.add(typeName);
          break;
          case CsnIterator.FORCE_TYPE:
            iterator.next();
            if (iterator.next() == CsnIterator.CLASS_NAME) {
              typeNames.add(iterator.getQNameValue());
            } else {
              throw new IllegalStateException("Expecting a class name");
            }
          break;
          case CsnIterator.EXCLUDE_TYPE:
            iterator.next();
            if (iterator.next() == CsnIterator.CLASS_NAME) {
              typeNames.add(iterator.getQNameValue());
            } else {
              throw new IllegalStateException("Expecting a class name");
            }
          break;
          case CsnIterator.VALUE:
            iterator.next();
            values.add(iterator.getValue());
          break;
          default:
            hasMore = false;
          break;
        }
      }
      attributeName = attributeName.replaceFirst("position.geometry",
        "position");
      final int dotIndex = attributeName.indexOf('.');
      if (dotIndex == -1) {
        final Attribute attribute = type.getAttribute(attributeName);
        if (attribute != null) {
          if (!typeNames.isEmpty()) {
            attribute.setProperty(AttributeProperties.ALLOWED_TYPE_NAMES,
              typeNames);
          }
          if (!values.isEmpty()) {
            attribute.setProperty(AttributeProperties.ALLOWED_VALUES, values);
          }
        }
      } else {
        final String key = attributeName.substring(0, dotIndex);
        final String subKey = attributeName.substring(dotIndex + 1);
        final Attribute attribute = type.getAttribute(key);
        if (attribute != null) {
          if (!typeNames.isEmpty()) {
            Map<String, List<QName>> allowedValues = attribute.getProperty(AttributeProperties.ATTRIBUTE_ALLOWED_TYPE_NAMES);
            if (allowedValues == null) {
              allowedValues = new HashMap<String, List<QName>>();
              attribute.setProperty(
                AttributeProperties.ATTRIBUTE_ALLOWED_TYPE_NAMES, allowedValues);
            }
            allowedValues.put(subKey, typeNames);
          }
          if (!values.isEmpty()) {
            Map<String, List<Object>> allowedValues = attribute.getProperty(AttributeProperties.ATTRIBUTE_ALLOWED_VALUES);
            if (allowedValues == null) {
              allowedValues = new HashMap<String, List<Object>>();
              attribute.setProperty(
                AttributeProperties.ATTRIBUTE_ALLOWED_VALUES, allowedValues);
            }
            allowedValues.put(subKey, values);
          }
        }
      }

    }
  }

  public void setCommonMetaDataProperties(
    final List<DataObjectMetaDataProperty> commonMetaDataProperties) {
    this.commonMetaDataProperties = commonMetaDataProperties;
  }

  private void setMetaDataProperties(final DataObjectMetaDataImpl metaData) {
    for (final DataObjectMetaDataProperty property : commonMetaDataProperties) {
      final DataObjectMetaDataProperty clonedProperty = property.clone();
      clonedProperty.setMetaData(metaData);
    }
  }

  public void subclass(final DataObjectMetaData type, final CsnIterator iterator)
    throws IOException {
    if (iterator.next() == CsnIterator.CLASS_NAME) {
      final QName className = iterator.getQNameValue();
      currentClass = new DataObjectMetaDataImpl(className);
      for (final DataObjectMetaData superClassDef : currentSuperClasses) {
        addSuperClass(currentClass, superClassDef);
      }
      // currentClass.setName(className);
      schema.addMetaData(currentClass);
    }
  }
}
