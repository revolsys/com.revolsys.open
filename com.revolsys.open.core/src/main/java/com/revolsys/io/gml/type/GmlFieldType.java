package com.revolsys.io.gml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.io.xml.XmlWriter;

public interface GmlFieldType {

  DataType getDataType();

  String getXmlSchemaTypeName();

  void writeValue(XmlWriter out, Object value);
}
