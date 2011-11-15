package com.revolsys.io.ecsv.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class ShortFieldType extends NumberFieldType {
   public ShortFieldType() {
    super(DataTypes.SHORT);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
       return Short.parseShort(text);
    } else {
      return null;
    }
}

}
