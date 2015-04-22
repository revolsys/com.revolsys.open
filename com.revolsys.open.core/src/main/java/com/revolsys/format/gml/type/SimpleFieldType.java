package com.revolsys.format.gml.type;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.xml.XmlWriter;

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
