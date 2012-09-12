package com.revolsys.gis.model.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.converter.ObjectConverter;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class SchemaMapper {
  private final Map<Attribute, Attribute> attributeMapping = new LinkedHashMap<Attribute, Attribute>();

  private final Map<DataObjectMetaData, ObjectConverter> typeConverter = new LinkedHashMap<DataObjectMetaData, ObjectConverter>();

  private final Map<DataObjectMetaData, DataObjectMetaData> typeMapping = new LinkedHashMap<DataObjectMetaData, DataObjectMetaData>();

  public SchemaMapper() {
  }

  /**
   * Add a forward and reverse mapping from one DataObjectMetaData to another.
   * 
   * @param from The type.
   * @param to The mapped type.
   */
  public void addAttributeMapping(final Attribute from, final Attribute to) {
    attributeMapping.put(from, to);
    attributeMapping.put(to, from);
  }

  /**
   * Add a forward and reverse mapping from one Attribute to another.
   * 
   * @param from The attribute.
   * @param to The mapped type.
   */
  public void addAttributeMapping(final DataObjectMetaData fromClass,
    final String fromName, final DataObjectMetaData toClass, final String toName) {
    final Attribute fromAttribute = fromClass.getAttribute(fromName);
    final Attribute toAttribute = toClass.getAttribute(toName);
    addAttributeMapping(fromAttribute, toAttribute);
  }

  /**
   * Add an object converter for the specified type.
   * 
   * @param from The type.
   * @param converter The converter.
   */
  public void addTypeConverter(final DataObjectMetaData type,
    final ObjectConverter converter) {
    typeConverter.put(type, converter);
  }

  /**
   * Add a forward and reverse mapping from one DataObjectMetaData to another.
   * 
   * @param from The type.
   * @param to The mapped type.
   */
  public void addTypeMapping(final DataObjectMetaData from,
    final DataObjectMetaData to) {
    typeMapping.put(from, to);
    typeMapping.put(to, from);
  }

  public DataObject convert(final DataObject object) {
    final DataObjectMetaData type = object.getMetaData();
    final DataObjectMetaData newType = getClassMapping(type);
    if (newType != null) {
      final DataObject newObject = newType.createDataObject();
      for (int i = 0; i < type.getAttributeCount(); i++) {
        final Attribute attribute = type.getAttribute(i);
        final Attribute newAttribute = getAttributeMapping(attribute);
        if (newAttribute != null) {
          final Object value = object.getValue(i);
          newObject.setValue(newAttribute.getName(), value);
        }
      }
      return newObject;
    } else {
      return object;
    }

  }

  /**
   * Get the Attribute that the specified attribute maps to.
   * 
   * @param attribute The attribute to map.
   * @return The mapped attribute.
   */
  public Attribute getAttributeMapping(final Attribute attribute) {
    return attributeMapping.get(attribute);
  }

  /**
   * Get the DataObjectMetaData that the specified class maps to.
   * 
   * @param type The class to map.
   * @return The mapped class.
   */
  public DataObjectMetaData getClassMapping(final DataObjectMetaData type) {
    return typeMapping.get(type);
  }

  /**
   * Get the converter that can convert objects of the specified type.
   * 
   * @param type The type to convert.
   * @return The converter
   */
  public ObjectConverter getTypeConverter(final DataObjectMetaData type) {
    return typeConverter.get(type);
  }

  /**
   * Get the DataObjectMetaData that the specified type maps to.
   * 
   * @param type The type to map.
   * @return The mapped type.
   */
  public DataObjectMetaData getTypeMapping(final DataObjectMetaData type) {
    return typeMapping.get(type);
  }

  /**
   * Check there is a converter for the type.
   * 
   * @param type The type to convert.
   * @return The converter
   */
  public boolean hasTypeConverter(final DataObjectMetaData type) {
    return typeConverter.containsKey(type);
  }
}
