package com.revolsys.gis.data.model;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.MathUtil;

/**
 * The Attribute class defines the name, type and other properties about each
 * attribute on a {@link DataObject} in the {@link DataObjectMetaData}.
 * 
 * @author Paul Austin
 * @see DataObject
 * @see DataObjectMetaData
 */
public class Attribute extends AbstractObjectWithProperties implements
  Cloneable {
  private final Map<Object, Object> allowedValues = new LinkedHashMap<Object, Object>();

  private Object defaultValue;

  /** The description of the attribute. */
  private String description;

  private int index;

  /** The maximum length of an attribute value. */
  private int length;

  /** The name of the attribute. */
  private String name;

  /** The flag indicating if a value is required for the attribute. */
  private boolean required;

  /** The maximum number of decimal places. */
  private int scale;

  /** The data type of the attribute value. */
  private DataType type;

  private Reference<DataObjectMetaData> metaData;

  private String title;

  private Object minValue;

  private Object maxValue;

  public Attribute() {
  }

  public Attribute(final Attribute attribute) {
    this.name = attribute.getName();
    this.title = attribute.getTitle();
    this.description = attribute.getDescription();
    this.type = attribute.getType();
    this.required = attribute.isRequired();
    this.length = attribute.getLength();
    this.scale = attribute.getScale();
    this.minValue = attribute.getMinValue();
    this.maxValue = attribute.getMaxValue();
    final Map<String, Object> properties = attribute.getProperties();
    setProperties(properties);
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
    final boolean required, final Map<String, Object> properties) {
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
    final boolean required, final Map<String, Object> properties) {
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
    final Map<String, Object> properties) {
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
    final String description) {
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
    final String description, final Map<String, Object> properties) {
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

  public void appendType(final StringBuffer string) {
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

  public Map<Object, Object> getAllowedValues() {
    return allowedValues;
  }

  @SuppressWarnings("unchecked")
  public <T> T getDefaultValue() {
    return (T)defaultValue;
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

  public int getMaxStringLength() {
    int length = this.length;
    if (scale > 0) {
      length += 1;
      length += scale;
    }
    if (Number.class.isAssignableFrom(type.getJavaClass())) {
      length += 1;
    } else if (DataTypes.DATE.equals(type)) {
      return 10;
    }
    return length;
  }

  @SuppressWarnings("unchecked")
  public <V> V getMaxValue() {
    return (V)maxValue;
  }

  public DataObjectMetaData getMetaData() {
    if (metaData == null) {
      return null;
    } else {
      return metaData.get();
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V getMinValue() {
    return (V)minValue;
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
   * Get the maximum number of decimal places of the attribute value.
   * 
   * @return The maximum number of decimal places.
   */
  public int getScale() {
    return scale;
  }

  public String getSimpleType() {
    final StringBuffer string = new StringBuffer();
    String typeName;
    if (Number.class.isAssignableFrom(getTypeClass())) {
      typeName = "NUMBER";
    } else if (CharSequence.class.isAssignableFrom(getTypeClass())) {
      typeName = "CHARACTER";
    } else {
      typeName = type.getName().toUpperCase();
    }
    string.append(typeName);
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

  public String getTitle() {
    return title;
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
   * Get the data type class of the attribute value.
   * 
   * @return The data type of the attribute value.
   */
  public Class<?> getTypeClass() {
    if (type == null) {
      return Object.class;
    } else {
      return type.getJavaClass();
    }
  }

  /**
   * Get the data type of the attribute value.
   * 
   * @return The data type of the attribute value.
   */
  public String getTypeDescription() {
    final StringBuffer typeDescription = new StringBuffer();
    appendType(typeDescription);
    return typeDescription.toString();
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

  public void setAllowedValues(final Collection<?> allowedValues) {
    for (final Object allowedValue : allowedValues) {
      this.allowedValues.put(allowedValue, allowedValue);
    }
  }

  public void setAllowedValues(final Map<?, ?> allowedValues) {
    this.allowedValues.putAll(allowedValues);
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

  protected void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = new WeakReference<DataObjectMetaData>(metaData);
  }

  public void setMinValue(final Object minValue) {
    this.minValue = minValue;
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

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer(name);
    string.append(':');
    appendType(string);
    return string.toString();
  }
}
