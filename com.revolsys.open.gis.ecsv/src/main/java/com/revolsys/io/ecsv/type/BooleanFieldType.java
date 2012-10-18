package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class BooleanFieldType extends AbstractEcsvFieldType {
  public BooleanFieldType() {
    super(DataTypes.BOOLEAN);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Boolean.parseBoolean(text);
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value != null) {
      out.print(value);
    }
  }
}
