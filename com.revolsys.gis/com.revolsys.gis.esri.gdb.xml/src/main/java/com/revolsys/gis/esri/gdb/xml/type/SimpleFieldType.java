package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.xml.io.XmlWriter;

public class SimpleFieldType extends AbstractEsriGeodatabaseXmlFieldType {

  private boolean usePrecision;

  private int fixedLength = -1;

  public SimpleFieldType(
    String esriFieldTypeName,
    DataType dataType,
    boolean usePrecision,
    int fixedLength) {
    super(dataType, "xs:" + dataType.getName().getLocalPart(),
      esriFieldTypeName);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  public SimpleFieldType(
    String esriFieldTypeName,
    DataType dataType,
    String xmlSchemaTypeName,
    boolean usePrecision,
    int fixedLength) {
    super(dataType, xmlSchemaTypeName, esriFieldTypeName);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  public SimpleFieldType(
    String esriFieldTypeName,
    DataType dataType,
    boolean usePrecision) {
    this(esriFieldTypeName, dataType, usePrecision, -1);
  }

  protected void writeValueText(
    XmlWriter out,
    Object value) {
    out.text(value);
  }

  @Override
  public int getFixedLength() {
    return fixedLength;
  }

  @Override
  public boolean isUsePrecision() {
    return usePrecision;
  }

}
