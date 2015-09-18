package com.revolsys.datatype;

import com.revolsys.converter.string.ObjectStringConverter;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;

public class SimpleDataType implements DataType {
  private final Class<?> javaClass;

  private final String name;

  private StringConverter<?> converter;

  public SimpleDataType(final String name, final Class<?> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
    this.converter = StringConverterRegistry.getInstance().getConverter(javaClass);
    if (this.converter == null) {
      this.converter = new ObjectStringConverter();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> StringConverter<V> getConverter() {
    return (StringConverter<V>)this.converter;
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
  public String toString() {
    return this.name.toString();
  }

}
