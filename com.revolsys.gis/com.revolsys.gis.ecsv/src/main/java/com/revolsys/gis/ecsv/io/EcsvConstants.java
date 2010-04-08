package com.revolsys.gis.ecsv.io;

import java.nio.charset.Charset;

import javax.xml.namespace.QName;

public interface EcsvConstants {
  String NS_PREFIX = "";

  String NS_URI = "";

  String ATTRIBUTE_HEADER_TYPES = "attributeHeaderTypes";

  QName ATTRIBUTE_LENGTH = new QName(NS_URI, "attributeLength", NS_PREFIX);

  QName ATTRIBUTE_NAME = new QName(NS_URI, "attributeName", NS_PREFIX);

  QName ATTRIBUTE_REQUIRED = new QName(NS_URI, "attributeRequired", NS_PREFIX);

  QName ATTRIBUTE_SCALE = new QName(NS_URI, "attributeScale", NS_PREFIX);

  QName ATTRIBUTE_TYPE = new QName(NS_URI, "attributeType", NS_PREFIX);

  Charset CHARACTER_SET = Charset.forName("UTF-8");

  String DESCRIPTION = "Enhanced CSV";

  char FIELD_SEPARATOR = ',';

  String FILE_EXTENSION = "ecsv";

  QName META_DATA = new QName(NS_URI, "metaData", NS_PREFIX);

  QName META_DATA_TYPE_NAME = new QName(NS_URI, "metaDataTypeName", NS_PREFIX);

  char QUOTE_CHARACTER = '"';

  String SRID = "srid";

  String TYPE_NAME = "typeName";

  String MEDIA_TYPE = "text/x-ecsv";
}
