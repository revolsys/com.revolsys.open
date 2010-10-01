package com.revolsys.gis.gml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.xml.io.XmlWriter;

public interface GmlFieldType {

  void writeValue(
    XmlWriter out,
    Object value);

  String getXmlSchemaTypeName();

  DataType getDataType();
}
