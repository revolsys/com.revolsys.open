package com.revolsys.io.gml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.gml.GmlConstants;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.io.xml.XsiConstants;

public abstract class AbstractGmlFieldType implements GmlFieldType,
  GmlConstants {

  private final String xmlSchemaTypeName;

  private final DataType dataType;

  public AbstractGmlFieldType(final DataType dataType,
    final String xmlSchemaTypeName) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
  }

  public DataType getDataType() {
    return dataType;
  }

  protected String getType(final Object value) {
    return xmlSchemaTypeName;
  }

  public String getXmlSchemaTypeName() {
    return xmlSchemaTypeName;
  }

  public void writeValue(final XmlWriter out, final Object value) {
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      writeValueText(out, value);
    }
  }

  protected abstract void writeValueText(XmlWriter out, Object value);

}
