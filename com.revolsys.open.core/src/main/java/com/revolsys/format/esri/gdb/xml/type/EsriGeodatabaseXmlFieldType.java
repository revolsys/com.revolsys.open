package com.revolsys.format.esri.gdb.xml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.format.xml.XmlWriter;

public interface EsriGeodatabaseXmlFieldType {

  DataType getDataType();

  FieldType getEsriFieldType();

  int getFixedLength();

  String getXmlSchemaTypeName();

  boolean isUsePrecision();

  void writeValue(XmlWriter out, Object value);

}
