package com.revolsys.io.gml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.xml.XmlWriter;

public class SimpleFieldType extends AbstractGmlFieldType {

  public SimpleFieldType(final DataType dataType) {
    super(dataType, "xs:" + dataType.getName());
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    out.text(value);
  }
}
