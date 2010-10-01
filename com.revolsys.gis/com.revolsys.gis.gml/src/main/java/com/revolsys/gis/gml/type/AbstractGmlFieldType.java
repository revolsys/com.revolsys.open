package com.revolsys.gis.gml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.gml.GmlConstants;
import com.revolsys.xml.XsiConstants;
import com.revolsys.xml.io.XmlWriter;

public abstract class AbstractGmlFieldType implements
  GmlFieldType, GmlConstants {

  private String xmlSchemaTypeName;

  private DataType dataType;


  public AbstractGmlFieldType(
    DataType dataType,
    String xmlSchemaTypeName) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
   }

  public void writeValue(
    XmlWriter out,
    Object value) {
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      writeValueText(out, value);
    }
  }

  protected String getType(
    Object value) {
    return xmlSchemaTypeName;
  }

  protected abstract void writeValueText(
    XmlWriter out,
    Object value);


  public String getXmlSchemaTypeName() {
    return xmlSchemaTypeName;
  }

  public DataType getDataType() {
    return dataType;
  }

}
