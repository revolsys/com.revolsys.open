package com.revolsys.io.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.io.XmlWriter;

public interface EsriGeodatabaseXmlFieldType {

  DataType getDataType();

  FieldType getEsriFieldType();

  int getFixedLength();

  String getXmlSchemaTypeName();

  boolean isUsePrecision();

  void writeValue(XmlWriter out, Object value);

}
