package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.xml.io.XmlWriter;

public interface EsriGeodatabaseXmlFieldType {

  void writeValue(
    XmlWriter out,
    Object value);

  String getEsriFieldTypeName();

  String getXmlSchemaTypeName();

  DataType getDataType();

  boolean isUsePrecision();

  int getFixedLength();

}
