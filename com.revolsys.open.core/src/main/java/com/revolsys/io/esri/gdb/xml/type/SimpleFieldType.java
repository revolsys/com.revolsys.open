package com.revolsys.io.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.XmlWriter;

public class SimpleFieldType extends AbstractEsriGeodatabaseXmlFieldType {

  private final boolean usePrecision;

  private int fixedLength = -1;

  public SimpleFieldType(final FieldType esriFieldType,
    final DataType dataType, final boolean usePrecision) {
    this(esriFieldType, dataType, usePrecision, -1);
  }

  public SimpleFieldType(final FieldType esriFieldType,
    final DataType dataType, final boolean usePrecision, final int fixedLength) {
    super(dataType, "xs:" + dataType.getName().getLocalPart(), esriFieldType);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  public SimpleFieldType(final FieldType esriFieldType,
    final DataType dataType, final String xmlSchemaTypeName,
    final boolean usePrecision, final int fixedLength) {
    super(dataType, xmlSchemaTypeName, esriFieldType);
    this.usePrecision = usePrecision;
    this.fixedLength = fixedLength;
  }

  @Override
  public int getFixedLength() {
    return fixedLength;
  }

  @Override
  public boolean isUsePrecision() {
    return usePrecision;
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    out.text(value);
  }

}
