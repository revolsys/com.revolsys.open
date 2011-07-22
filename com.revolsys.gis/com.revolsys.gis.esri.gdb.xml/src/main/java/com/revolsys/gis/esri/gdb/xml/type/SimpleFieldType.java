package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.xml.io.XmlWriter;

public class SimpleFieldType extends AbstractEsriGeodatabaseXmlFieldType {

  private boolean usePrecision;

  private int fixedLength = -1;

  public SimpleFieldType(
    FieldType esriFieldType,
    DataType dataType,
    boolean usePrecision,
    int fixedLength) {
    super(dataType, "xs:" + dataType.getName().getLocalPart(),
      esriFieldType);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  public SimpleFieldType(
    FieldType esriFieldType,
    DataType dataType,
    String xmlSchemaTypeName,
    boolean usePrecision,
    int fixedLength) {
    super(dataType, xmlSchemaTypeName, esriFieldType);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  public SimpleFieldType(
    FieldType esriFieldType,
    DataType dataType,
    boolean usePrecision) {
    this(esriFieldType, dataType, usePrecision, -1);
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
