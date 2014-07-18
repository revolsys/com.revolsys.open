package com.revolsys.io.gml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.xml.XmlWriter;

public class SimpleFieldType extends AbstractGmlFieldType {

  public static final SimpleFieldType OBJECT = new SimpleFieldType(
    DataTypes.OBJECT);

  public SimpleFieldType(final DataType dataType) {
    super(dataType, "xs:" + dataType.getName());
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    out.text(value);
  }
}
