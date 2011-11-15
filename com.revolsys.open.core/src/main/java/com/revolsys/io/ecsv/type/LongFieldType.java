package com.revolsys.io.ecsv.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class LongFieldType extends NumberFieldType {
   public LongFieldType() {
    super(DataTypes.LONG);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
        return Long.parseLong(text);
    } else {
      return null;
    }
}

}
