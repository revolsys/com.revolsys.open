package com.revolsys.io.ecsv.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class IntFieldType extends NumberFieldType {
  public IntFieldType() {
    super(DataTypes.INT);
  }

  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Integer.parseInt(text);
    } else {
      return null;
    }
  }

}
