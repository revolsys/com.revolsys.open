package com.revolsys.gis.data.model.types;

import javax.xml.namespace.QName;

public class SimpleDataType implements DataType {
  private final Class<?> javaClass;

  private final QName name;

  public SimpleDataType(
    final QName name,
    final Class<?> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
  }

  public SimpleDataType(
    final String name,
    final Class<?> javaClass) {
    this.name = QName.valueOf(name);
    this.javaClass = javaClass;
  }

  public Class<?> getJavaClass() {
    return javaClass;
  }

  public QName getName() {
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
