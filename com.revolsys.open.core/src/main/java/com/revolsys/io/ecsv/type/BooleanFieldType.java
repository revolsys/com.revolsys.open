package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class BooleanFieldType extends AbstractEcsvFieldType {
  public BooleanFieldType() {
    super(DataTypes.BOOLEAN);
  }

  public void writeValue(
    PrintWriter out,
    Object value) {
    if (value != null) {
      out.print(value);
    }
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
      return Boolean.parseBoolean(text);
    } else {
      return null;
    }
  }
}
