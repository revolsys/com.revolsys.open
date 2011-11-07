package com.revolsys.gis.ecsv.io.type;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class IntegerFieldType extends NumberFieldType {
   public IntegerFieldType() {
    super(DataTypes.INTEGER);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
       return new BigInteger(text);
    } else {
      return null;
    }
 }

}
