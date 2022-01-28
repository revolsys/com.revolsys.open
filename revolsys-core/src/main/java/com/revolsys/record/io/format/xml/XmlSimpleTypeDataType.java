package com.revolsys.record.io.format.xml;

import org.jeometry.common.data.type.DataType;

public class XmlSimpleTypeDataType extends XmlSimpleType {

  private final DataType dataType;

  public XmlSimpleTypeDataType(final XmlName xmlName, final DataType dataType) {
    super(xmlName);
    this.dataType = dataType;
  }

  public XmlSimpleTypeDataType(final XmlNamespace namespace, final String localPart,
    final DataType dataType) {
    this(namespace.getName(localPart), dataType);
  }

  public DataType getDataType() {
    return this.dataType;
  }

  @Override
  public <V> V toValue(final String text) {
    return this.dataType.toObject(text);
  }
}
