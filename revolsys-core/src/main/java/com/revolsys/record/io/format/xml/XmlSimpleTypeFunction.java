package com.revolsys.record.io.format.xml;

import java.util.function.Function;

public class XmlSimpleTypeFunction<T> extends XmlSimpleType {

  public static <T2> XmlSimpleTypeFunction<T2> create(final XmlNamespace namespace,
    final String name, final Function<String, T2> converter) {
    return new XmlSimpleTypeFunction<T2>(namespace, name, converter);
  }

  private Function<String, T> converter;

  public XmlSimpleTypeFunction(final XmlName xmlName, final Function<String, T> converter) {
    super(xmlName);
    this.converter = converter;
  }

  public XmlSimpleTypeFunction(final XmlNamespace namespace, final String localPart) {
    super(namespace.getName(localPart));
  }

  public XmlSimpleTypeFunction(final XmlNamespace namespace, final String localPart,
    final Function<String, T> converter) {
    this(namespace.getName(localPart), converter);
  }

  public Function<String, T> getConverter() {
    return this.converter;
  }

  public void setConverter(final Function<String, T> converter) {
    if (this.converter != null) {
      throw new IllegalStateException("Cannot modify the converter");
    }
    this.converter = converter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toValue(final String text) {
    return (V)this.converter.apply(text);
  }
}
