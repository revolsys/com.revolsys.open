package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.util.Base64;
import com.revolsys.util.Base64EncodingWriter;

public class Base64BinaryFieldType extends AbstractEcsvFieldType {

  public Base64BinaryFieldType() {
    super(DataTypes.BASE64_BINARY);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return Base64.decode(text);
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value != null) {
      final Base64EncodingWriter base64Out = new Base64EncodingWriter(out);
      if (value instanceof byte[]) {
        final byte[] bytes = (byte[])value;
        base64Out.print(bytes);
      } else {
        base64Out.print(value);
      }
      base64Out.flush();
    }
  }

}
