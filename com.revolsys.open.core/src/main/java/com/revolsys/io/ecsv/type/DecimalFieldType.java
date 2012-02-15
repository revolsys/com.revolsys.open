package com.revolsys.io.ecsv.type;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class DecimalFieldType extends NumberFieldType {
  public DecimalFieldType() {
    super(DataTypes.DECIMAL);
  }

  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return new BigDecimal(text);
    } else {
      return null;
    }
  }

}
