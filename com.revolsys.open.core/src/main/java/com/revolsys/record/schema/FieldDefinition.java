package com.revolsys.record.schema;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.collection.map.Maps;
import com.revolsys.comparator.NumericComparator;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.code.CodeTableProperty;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

/**
 * The FieldDefinition class defines the name, type and other properties about each
 * field on a {@link Record} in the {@link RecordDefinition}.
 *
 * @see Record
 * @see RecordDefinition
 */
public class FieldDefinition extends BaseObjectWithProperties implements Cloneable, MapSerializer {
  public static FieldDefinition newFieldDefinition(final Map<String, Object> properties) {
    return new FieldDefinition(properties);
  }

  private final Map<Object, Object> allowedValues = new LinkedHashMap<Object, Object>();

  private CodeTable codeTable;

  private Object defaultValue;

  /** The description of the field. */
  private String description;

  private int index;

  /** The maximum length of an field value. */
  private int length;

  private Object maxValue;

  private Object minValue;

  /** The name of the field. */
  private String name;

  private Reference<RecordDefinition> recordDefinition;

  /** The flag indicating if a value is required for the field. */
  private boolean required;

  /** The maximum number of decimal places. */
  private int scale;

  private String title;

  /** The data type of the field value. */
  private DataType type;

  public FieldDefinition() {
  }

  public FieldDefinition(final FieldDefinition field) {
    this.name = field.getName();
    this.title = field.getTitle();
    this.description = field.getDescription();
    this.type = field.getType();
    this.required = field.isRequired();
    this.length = field.getLength();
    this.scale = field.getScale();
    this.minValue = field.getMinValue();
    this.maxValue = field.getMaxValue();
    final Map<String, Object> properties = field.getProperties();
    setProperties(properties);
  }

  public FieldDefinition(final int index) {
    this.index = index;
  }

  public FieldDefinition(final Map<String, Object> properties) {
    this.name = Maps.getString(properties, "name");
    this.title = Maps.getString(properties, "title");
    if (!Property.hasValue(this.title)) {
      this.title = CaseConverter.toCapitalizedWords(this.name);
    }
    this.description = Maps.getString(properties, "description");
    this.type = DataTypes.getType(Maps.getString(properties, "dataType"));
    this.required = Maps.getBool(properties, "required");
    this.length = Maps.getInteger(properties, "length", 0);
    this.scale = Maps.getInteger(properties, "scale", 0);
    this.minValue = properties.get("minValue");
    if (this.minValue == null) {
      this.minValue = MathUtil.getMinValue(getTypeClass());
    } else {
      this.minValue = StringConverter.toString(this.type, this.minValue);
    }
    if (this.maxValue == null) {
      this.maxValue = MathUtil.getMaxValue(getTypeClass());
    } else {
      this.maxValue = StringConverter.toString(this.type, this.maxValue);
    }
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param required The flag indicating if a value is required for the
   *          field.
   */
  public FieldDefinition(final String name, final DataType type, final boolean required) {
    this(name, type, 0, 0, required, null, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final boolean required,
    final Map<String, Object> properties) {
    this(name, type, 0, 0, required, properties);
  }

  public FieldDefinition(final String name, final DataType dataType, final boolean required,
    final String description) {
    this(name, dataType, 0, 0, required, description, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param required The flag indicating if a value is required for the
   *          field.
   */
  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required) {
    this(name, type, length, 0, required, null, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required, final Map<String, Object> properties) {
    this(name, type, length, 0, required, properties);
  }

  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required, final String description) {
    this(name, type, length, 0, required, description, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required) {
    this(name, type, length, scale, required, "");
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required, final Map<String, Object> properties) {
    this(name, type, length, scale, required, null, properties);

  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required, final String description) {
    this.name = name;
    this.title = CaseConverter.toCapitalizedWords(name);
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
    this.minValue = MathUtil.getMinValue(getTypeClass());
    this.maxValue = MathUtil.getMaxValue(getTypeClass());
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required, final String description,
    final Map<String, Object> properties) {
    this.name = name;
    this.title = CaseConverter.toCapitalizedWords(name);
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
    final Class<?> typeClass = getTypeClass();
    this.minValue = MathUtil.getMinValue(typeClass);
    this.maxValue = MathUtil.getMaxValue(typeClass);
    setProperties(properties);
  }

  public void addAllowedValue(final Object value, final Object text) {
    this.allowedValues.put(value, text);
  }

  public void appendType(final StringBuilder string) {
    string.append(this.type);
    if (this.length > 0) {
      string.append('(');
      string.append(this.length);
      if (this.scale > 0) {
        string.append(',');
        string.append(this.scale);
      }
      string.append(')');
    }
  }

  @Override
  public FieldDefinition clone() {
    return new FieldDefinition(this);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof FieldDefinition) {
      final FieldDefinition fieldDefinition = (FieldDefinition)object;
      return this.name.equals(fieldDefinition.getName());
    } else {
      return false;
    }
  }

  public Map<Object, Object> getAllowedValues() {
    return this.allowedValues;
  }

  public CodeTable getCodeTable() {
    return this.codeTable;
  }

  @SuppressWarnings("unchecked")
  public <T> T getDefaultValue() {
    return (T)this.defaultValue;
  }

  public String getDescription() {
    return this.description;
  }

  public int getIndex() {
    return this.index;
  }

  /**
   * Get the maximum length of the field value. The length 0 should be used
   * if there is no maximum.
   *
   * @return The maximum length of an field value.
   */
  public int getLength() {
    return this.length;
  }

  public int getMaxStringLength() {
    int length = this.length;
    if (this.scale > 0) {
      length += 1;
      length += this.scale;
    }
    if (Number.class.isAssignableFrom(this.type.getJavaClass())) {
      length += 1;
    } else if (DataTypes.DATE.equals(this.type)) {
      return 10;
    }
    return length;
  }

  @SuppressWarnings("unchecked")
  public <V> V getMaxValue() {
    return (V)this.maxValue;
  }

  @SuppressWarnings("unchecked")
  public <V> V getMinValue() {
    return (V)this.minValue;
  }

  /**
   * Get the name of the field.
   *
   * @return The name of the field.
   */
  public String getName() {
    return this.name;
  }

  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      return null;
    } else {
      return this.recordDefinition.get();
    }
  }

  /**
   * Get the maximum number of decimal places of the field value.
   *
   * @return The maximum number of decimal places.
   */
  public int getScale() {
    return this.scale;
  }

  public String getSimpleType() {
    final StringBuilder string = new StringBuilder();
    String typeName;
    if (Number.class.isAssignableFrom(getTypeClass())) {
      typeName = "NUMBER";
    } else if (CharSequence.class.isAssignableFrom(getTypeClass())) {
      typeName = "CHARACTER";
    } else {
      typeName = this.type.getName().toUpperCase();
    }
    string.append(typeName);
    if (this.length > 0) {
      string.append('(');
      string.append(this.length);
      if (this.scale > 0) {
        string.append(',');
        string.append(this.scale);
      }
      string.append(')');
    }
    return string.toString();
  }

  public String getTitle() {
    return this.title;
  }

  /**
   * Get the data type of the field value.
   *
   * @return The data type of the field value.
   */
  public DataType getType() {
    return this.type;
  }

  /**
   * Get the data type class of the field value.
   *
   * @return The data type of the field value.
   */
  public Class<?> getTypeClass() {
    if (this.type == null) {
      return Object.class;
    } else {
      return this.type.getJavaClass();
    }
  }

  /**
   * Get the data type of the field value.
   *
   * @return The data type of the field value.
   */
  public String getTypeDescription() {
    final StringBuilder typeDescription = new StringBuilder();
    appendType(typeDescription);
    return typeDescription.toString();
  }

  /**
   * Return the hash code of the field.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  /**
   * Get the flag indicating if a value is required for the field.
   *
   * @return True if a value is required, false otherwise.
   */
  public boolean isRequired() {
    return this.required;
  }

  public boolean isValid(final Object value) {
    try {
      validate(value);
      return true;
    } catch (final Throwable e) {
      return false;
    }

  }

  public void setAllowedValues(final Collection<?> allowedValues) {
    for (final Object allowedValue : allowedValues) {
      this.allowedValues.put(allowedValue, allowedValue);
    }
  }

  public void setAllowedValues(final Map<?, ?> allowedValues) {
    this.allowedValues.putAll(allowedValues);
  }

  public void setCodeTable(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  public void setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  void setIndex(final int index) {
    this.index = index;
  }

  public void setLength(final int length) {
    this.length = length;
  }

  public void setMaxValue(final Object maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(final Object minValue) {
    this.minValue = minValue;
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = new WeakReference<RecordDefinition>(recordDefinition);
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }

  public void setScale(final int scale) {
    this.scale = scale;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setType(final DataType type) {
    this.type = type;
  }

  public void setValue(final Record record, Object value) {
    if (record != null) {
      final int index = getIndex();
      value = toFieldValue(value);
      record.setValue(index, value);
    }
  }

  public void setValueClone(final Record record, Object value) {
    if (record != null) {
      final int index = getIndex();
      value = toFieldValue(value);
      value = JavaBeanUtil.clone(value);
      record.setValue(index, value);
    }
  }

  /**
   * Convert the object to a value that is valid for the field. If the value can't be converted then
   * the original value will be returned. This can result in invalid values in the record but those can be picked up
   * with validation. Otherwise invalid values would silently be removed.
   *
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V> V toFieldValue(final Object value) {
    try {
      return toFieldValueException(value);
    } catch (final Throwable e) {
      return (V)value;
    }
  }

  public <V> V toFieldValueException(Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        if (this.codeTable != null) {
          final Identifier identifier = this.codeTable.getIdentifier(value);
          if (identifier != null) {
            value = identifier.toSingleValue();
          }
        }
        V fieldValue = this.type.toObject(value);
        if (value instanceof String) {
          final String string = (String)value;
          if (!Property.hasValue(string)) {
            fieldValue = null;
          }
        }
        return fieldValue;
      } catch (final Throwable e) {
        throw new IllegalArgumentException(
          getName() + "='" + value + "' is not a valid " + getType().getValidationName(), e);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("type", "field");
    map.put("name", getName());
    map.put("title", getTitle());
    MapSerializerUtil.add(map, "description", getDescription(), "");
    map.put("dataType", getType().getName());
    map.put("length", getLength());
    map.put("scale", getScale());
    map.put("required", isRequired());
    MapSerializerUtil.add(map, "minValue", getMinValue(), null);
    MapSerializerUtil.add(map, "maxValue", getMaxValue(), null);
    MapSerializerUtil.add(map, "defaultValue", getDefaultValue(), null);
    MapSerializerUtil.add(map, "allowedValues", getAllowedValues(), Collections.emptyMap());
    return map;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(this.name);
    string.append(':');
    appendType(string);
    return string.toString();
  }

  public Object validate(Object value) {
    final String fieldName = getName();
    value = toFieldValueException(value);
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(fieldName + " is required");
      }
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
      if (codeTable == null) {
        final int maxLength = getLength();
        if (value instanceof Number) {
          final Number number = (Number)value;
          final BigDecimal bigNumber = new BigDecimal(number.toString());
          final int length = bigNumber.precision();
          if (maxLength > 0) {
            if (length > maxLength) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " length " + length + " > " + maxLength);
            }
          }

          final int scale = bigNumber.scale();
          final int maxScale = getScale();
          if (maxScale > 0) {
            if (scale > maxScale) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " scale " + scale + " > " + maxScale);
            }
          }
          final Number minValue = getMinValue();
          if (minValue != null) {
            if (NumericComparator.numericCompare(number, minValue) < 0) {
              throw new IllegalArgumentException(fieldName + "=" + value + " > " + minValue);
            }
          }
          final Number maxValue = getMaxValue();
          if (maxValue != null) {
            if (NumericComparator.numericCompare(number, maxValue) > 0) {
              throw new IllegalArgumentException(fieldName + "=" + value + " < " + maxValue);
            }
          }
        } else if (value instanceof String) {
          final String string = (String)value;
          final int length = string.length();
          if (maxLength > 0) {
            if (length > maxLength) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " length " + length + " > " + maxLength);
            }
          }
        }
        if (!this.allowedValues.isEmpty()) {
          if (!this.allowedValues.containsKey(value)) {
            throw new IllegalArgumentException(fieldName + "=" + value + " not in ("
              + Strings.toString(",", this.allowedValues) + ")");
          }
        }
      } else {
        final Identifier id = codeTable.getIdentifier(value);
        if (id == null) {
          String codeTableName;
          if (codeTable instanceof CodeTableProperty) {
            @SuppressWarnings("resource")
            final CodeTableProperty property = (CodeTableProperty)codeTable;
            codeTableName = property.getTypeName();
          } else {
            codeTableName = codeTable.toString();
          }
          throw new IllegalArgumentException(
            "Unable to find code for '" + value + "' in " + codeTableName);
        }
      }
    }
    return value;
  }

  public Object validate(final Record record, final Object value) {
    final String fieldName = getName();
    try {
      return validate(value);
    } catch (final Throwable e) {
      throw new ObjectPropertyException(record, fieldName, e.getMessage(), e);
    }
  }
}
