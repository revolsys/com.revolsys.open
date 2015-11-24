package com.revolsys.datatype;

public abstract class AbstractDataType implements DataType {

  private final Class<?> javaClass;

  private final String name;

  private final boolean requiresQuotes;

  public AbstractDataType(final String name, final Class<?> javaClass,
    final boolean requiresQuotes) {
    this.name = name;
    this.javaClass = javaClass;
    this.requiresQuotes = requiresQuotes;
    DataTypes.register(this);
  }

  @Override
  public Class<?> getJavaClass() {
    return this.javaClass;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getValidationName() {
    if (Number.class.isAssignableFrom(this.javaClass)) {
      return "number (" + getName() + ")";
    } else {
      return getName();
    }
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean isRequiresQuotes() {
    return this.requiresQuotes;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Class<?> javaClass = getJavaClass();
      final Class<?> valueClass = value.getClass();
      if (javaClass.isAssignableFrom(valueClass)) {
        return (V)value;
      } else {
        return (V)toObjectDo(value);
      }
    }
  }

  protected Object toObjectDo(final Object value) {
    throw new IllegalArgumentException(value + " is not a valid " + getValidationName());
  }

  @Override
  public String toString() {
    return this.name.toString();
  }
}
