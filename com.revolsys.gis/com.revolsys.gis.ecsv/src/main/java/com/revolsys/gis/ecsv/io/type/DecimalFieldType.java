package com.revolsys.gis.ecsv.io.type;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class DecimalFieldType extends NumberFieldType {
   public DecimalFieldType() {
    super(DataTypes.DECIMAL);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
         return new BigDecimal(text);
    } else {
      return null;
    }
 }

}
