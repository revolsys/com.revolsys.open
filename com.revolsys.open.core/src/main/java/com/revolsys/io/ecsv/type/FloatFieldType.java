package com.revolsys.io.ecsv.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class FloatFieldType extends NumberFieldType {
  public FloatFieldType() {
    super(DataTypes.FLOAT);
  }

  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Float.parseFloat(text);
    } else {
      return null;
    }
  }

}
