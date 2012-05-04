package com.revolsys.gis.data.model.types;

public class SimpleDataType implements DataType {
  private final Class<?> javaClass;

  private final String name;

  public SimpleDataType(final String name, final Class<?> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
  }

  public Class<?> getJavaClass() {
    return javaClass;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name.toString();
  }

}
