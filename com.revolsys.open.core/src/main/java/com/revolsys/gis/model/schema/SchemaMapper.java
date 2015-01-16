package com.revolsys.gis.model.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.converter.ObjectConverter;

public class SchemaMapper {
  private final Map<FieldDefinition, FieldDefinition> attributeMapping = new LinkedHashMap<>();

  private final Map<RecordDefinition, ObjectConverter> typeConverter = new LinkedHashMap<>();

  private final Map<RecordDefinition, RecordDefinition> typeMapping = new LinkedHashMap<>();

  public SchemaMapper() {
  }

  /**
   * Add a forward and reverse mapping from one RecordDefinition to another.
   *
   * @param from The type.
   * @param to The mapped type.
   */
  public void addFieldMapping(final FieldDefinition from,
    final FieldDefinition to) {
    this.attributeMapping.put(from, to);
    this.attributeMapping.put(to, from);
  }

  /**
   * Add a forward and reverse mapping from one FieldDefinition to another.
   *
   * @param from The attribute.
   * @param to The mapped type.
   */
  public void addFieldMapping(final RecordDefinition fromClass,
    final String fromName, final RecordDefinition toClass, final String toName) {
    final FieldDefinition fromAttribute = fromClass.getField(fromName);
    final FieldDefinition toAttribute = toClass.getField(toName);
    addFieldMapping(fromAttribute, toAttribute);
  }

  /**
   * Add an object converter for the specified type.
   *
   * @param from The type.
   * @param converter The converter.
   */
  public void addTypeConverter(final RecordDefinition type,
    final ObjectConverter converter) {
    this.typeConverter.put(type, converter);
  }

  /**
   * Add a forward and reverse mapping from one RecordDefinition to another.
   *
   * @param from The type.
   * @param to The mapped type.
   */
  public void addTypeMapping(final RecordDefinition from,
    final RecordDefinition to) {
    this.typeMapping.put(from, to);
    this.typeMapping.put(to, from);
  }

  public Record convert(final Record object) {
    final RecordDefinition type = object.getRecordDefinition();
    final RecordDefinition newType = getClassMapping(type);
    if (newType != null) {
      final Record newObject = newType.createRecord();
      for (int i = 0; i < type.getFieldCount(); i++) {
        final FieldDefinition attribute = type.getField(i);
        final FieldDefinition newAttribute = getFieldMapping(attribute);
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
   * Get the RecordDefinition that the specified class maps to.
   *
   * @param type The class to map.
   * @return The mapped class.
   */
  public RecordDefinition getClassMapping(final RecordDefinition type) {
    return this.typeMapping.get(type);
  }

  /**
   * Get the FieldDefinition that the specified attribute maps to.
   *
   * @param attribute The attribute to map.
   * @return The mapped attribute.
   */
  public FieldDefinition getFieldMapping(final FieldDefinition attribute) {
    return this.attributeMapping.get(attribute);
  }

  /**
   * Get the converter that can convert objects of the specified type.
   *
   * @param type The type to convert.
   * @return The converter
   */
  public ObjectConverter getTypeConverter(final RecordDefinition type) {
    return this.typeConverter.get(type);
  }

  /**
   * Get the RecordDefinition that the specified type maps to.
   *
   * @param type The type to map.
   * @return The mapped type.
   */
  public RecordDefinition getTypeMapping(final RecordDefinition type) {
    return this.typeMapping.get(type);
  }

  /**
   * Check there is a converter for the type.
   *
   * @param type The type to convert.
   * @return The converter
   */
  public boolean hasTypeConverter(final RecordDefinition type) {
    return this.typeConverter.containsKey(type);
  }
}
