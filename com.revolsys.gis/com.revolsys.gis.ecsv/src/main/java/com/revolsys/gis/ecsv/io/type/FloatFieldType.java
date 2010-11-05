package com.revolsys.gis.ecsv.io.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class FloatFieldType extends NumberFieldType {
   public FloatFieldType(
    ) {
    super(DataTypes.FLOAT);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
        return Float.parseFloat(text);
    } else {
      return null;
    }
}

}
