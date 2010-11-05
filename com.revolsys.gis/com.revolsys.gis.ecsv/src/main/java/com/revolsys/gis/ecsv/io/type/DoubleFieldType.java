package com.revolsys.gis.ecsv.io.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class DoubleFieldType extends NumberFieldType {
   public DoubleFieldType(
    ) {
    super(DataTypes.DOUBLE);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
        return Double.parseDouble(text);
    } else {
      return null;
    }
 }

}
