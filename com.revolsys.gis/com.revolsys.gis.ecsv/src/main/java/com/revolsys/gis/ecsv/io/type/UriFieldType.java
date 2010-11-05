package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class UriFieldType extends AbstractEcsvFieldType {
  public UriFieldType() {
    super(DataTypes.ANY_URI);

  }

  public void writeValue(
    final PrintWriter out,
    final Object value) {
    StringFieldType.writeQuotedString(out, value);
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
       try {
      return new URI(text);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(text + " is not a valid URI", e);
    }
    } else {
      return null;
    }
  }
}
