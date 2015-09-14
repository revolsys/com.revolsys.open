package com.revolsys.datatype;

public class SimpleDataType implements DataType {
  private final Class<?> javaClass;

  private final String name;

  public SimpleDataType(final String name, final Class<?> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
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
