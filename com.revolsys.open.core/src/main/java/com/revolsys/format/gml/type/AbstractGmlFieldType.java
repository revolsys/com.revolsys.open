package com.revolsys.format.gml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.format.gml.GmlConstants;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.format.xml.XsiConstants;

public abstract class AbstractGmlFieldType implements GmlFieldType, GmlConstants {

  private final String xmlSchemaTypeName;

  private final DataType dataType;

  public AbstractGmlFieldType(final DataType dataType, final String xmlSchemaTypeName) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
  }

  @Override
  public DataType getDataType() {
    return this.dataType;
  }

  protected String getType(final Object value) {
    return this.xmlSchemaTypeName;
  }

  @Override
  public String getXmlSchemaTypeName() {
    return this.xmlSchemaTypeName;
  }

  @Override
  public void writeValue(final XmlWriter out, final Object value) {
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      writeValueText(out, value);
    }
  }

  protected abstract void writeValueText(XmlWriter out, Object value);

}
