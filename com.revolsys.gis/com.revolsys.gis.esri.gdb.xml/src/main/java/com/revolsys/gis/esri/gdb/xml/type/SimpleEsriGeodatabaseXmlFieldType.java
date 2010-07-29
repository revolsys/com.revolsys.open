package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.xml.io.XmlWriter;

public class SimpleEsriGeodatabaseXmlFieldType extends
  AbstractEsriGeodatabaseXmlFieldType {
  public SimpleEsriGeodatabaseXmlFieldType(
    String esriFieldTypeName,
    DataType dataType) {
    super(dataType, "xs:" + dataType.getName().getLocalPart(),
      esriFieldTypeName);
  }

  protected void writeValueText(
    XmlWriter out,
    Object value) {
    out.text(value);
  }
}
