package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.util.Base64;
import com.revolsys.util.Base64EncodingWriter;

public class Base64BinaryFieldType extends AbstractEcsvFieldType {

  public Base64BinaryFieldType() {
    super(DataTypes.BASE64_BINARY);
  }

  public void writeValue(
    PrintWriter out,
    Object value) {
    if (value != null) {
      Base64EncodingWriter base64Out = new Base64EncodingWriter(out);
      if (value instanceof byte[]) {
        byte[] bytes = (byte[])value;
        base64Out.print(bytes);
      } else {
        base64Out.print(value);
      }
      base64Out.flush();
    }
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
      return Base64.decode(text);
    } else {
      return null;
    }
  }

}
