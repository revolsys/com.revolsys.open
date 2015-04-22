package com.revolsys.format.gml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.format.xml.XmlWriter;

public interface GmlFieldType {

  DataType getDataType();

  String getXmlSchemaTypeName();

  void writeValue(XmlWriter out, Object value);
}
