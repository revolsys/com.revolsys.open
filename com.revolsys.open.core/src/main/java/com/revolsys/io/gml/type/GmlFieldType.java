package com.revolsys.io.gml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.xml.XmlWriter;

public interface GmlFieldType {

  void writeValue(
    XmlWriter out,
    Object value);

  String getXmlSchemaTypeName();

  DataType getDataType();
}
