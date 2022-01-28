package com.revolsys.record.io.format.xml;

import java.util.Base64;

import org.jeometry.common.data.type.DataTypes;

public interface XSD {

  XmlSimpleType STRING = new XmlSimpleTypeDataType(XmlConstants.XSD, "string", DataTypes.STRING);

  XmlSimpleType base64Binary = new XmlSimpleTypeFunction<byte[]>(XmlConstants.XSD, "base64Binary",
    Base64.getDecoder()::decode);

  XmlSimpleType DATE_TIME = new XmlSimpleTypeDataType(XmlConstants.XSD, "dateTime",
    DataTypes.INSTANT);

  XmlSimpleType INTEGER = new XmlSimpleTypeDataType(XmlConstants.XSD, "integer", DataTypes.INT);

  XmlSimpleType UNSIGNED_LONG = new XmlSimpleTypeDataType(XmlConstants.XSD, "unsignedLong",
    DataTypes.LONG);

}
