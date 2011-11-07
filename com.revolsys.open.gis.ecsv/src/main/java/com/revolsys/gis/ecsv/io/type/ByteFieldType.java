package com.revolsys.gis.ecsv.io.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class ByteFieldType extends NumberFieldType {
   public ByteFieldType(
   ) {
    super(DataTypes.BYTE);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
        return Byte.parseByte(text);
    } else {
      return null;
    }
 }

}
