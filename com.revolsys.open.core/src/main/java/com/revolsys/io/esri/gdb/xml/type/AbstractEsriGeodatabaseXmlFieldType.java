package com.revolsys.io.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.io.xml.XsiConstants;

public abstract class AbstractEsriGeodatabaseXmlFieldType implements
  EsriGeodatabaseXmlFieldType, EsriGeodatabaseXmlConstants {

  private final FieldType esriFieldType;

  private final String xmlSchemaTypeName;

  private final DataType dataType;

  public AbstractEsriGeodatabaseXmlFieldType(final DataType dataType,
    final String xmlSchemaTypeName, final FieldType esriFieldType) {
    this.dataType = dataType;
    this.xmlSchemaTypeName = xmlSchemaTypeName;
    this.esriFieldType = esriFieldType;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public FieldType getEsriFieldType() {
    return esriFieldType;
  }

  @Override
  public int getFixedLength() {
    return -1;
  }

  protected String getType(final Object value) {
    return xmlSchemaTypeName;
  }

  @Override
  public String getXmlSchemaTypeName() {
    return xmlSchemaTypeName;
  }

  @Override
  public boolean isUsePrecision() {
    return false;
  }

  @Override
  public void writeValue(final XmlWriter out, final Object value) {
    out.startTag(EsriGeodatabaseXmlConstants.VALUE);
    if (value == null) {
      out.attribute(XsiConstants.NIL, true);
    } else {
      out.attribute(XsiConstants.TYPE, getType(value));
      writeValueText(out, value);
    }
    out.endTag(EsriGeodatabaseXmlConstants.VALUE);
  }

  protected abstract void writeValueText(XmlWriter out, Object value);

}
