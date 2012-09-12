package com.revolsys.io.ecsv.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class DoubleFieldType extends NumberFieldType {
  public DoubleFieldType() {
    super(DataTypes.DOUBLE);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Double.parseDouble(text);
    } else {
      return null;
    }
  }

}
