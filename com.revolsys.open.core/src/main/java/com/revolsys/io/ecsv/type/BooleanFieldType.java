package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class BooleanFieldType extends AbstractEcsvFieldType {
  public BooleanFieldType() {
    super(DataTypes.BOOLEAN);
  }

  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Boolean.parseBoolean(text);
    } else {
      return null;
    }
  }

  public void writeValue(final PrintWriter out, final Object value) {
    if (value != null) {
      out.print(value);
    }
  }
}
