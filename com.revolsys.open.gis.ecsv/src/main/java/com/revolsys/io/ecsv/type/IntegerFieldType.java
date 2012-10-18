package com.revolsys.io.ecsv.type;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class IntegerFieldType extends NumberFieldType {
  public IntegerFieldType() {
    super(DataTypes.INTEGER);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return new BigInteger(text);
    } else {
      return null;
    }
  }

}
