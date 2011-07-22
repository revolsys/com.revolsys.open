package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.xml.io.XmlWriter;

public interface EsriGeodatabaseXmlFieldType {

  void writeValue(
    XmlWriter out,
    Object value);

  FieldType getEsriFieldType();

  String getXmlSchemaTypeName();

  DataType getDataType();

  boolean isUsePrecision();

  int getFixedLength();

}
