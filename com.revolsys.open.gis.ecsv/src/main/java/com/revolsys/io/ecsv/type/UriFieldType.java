package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class UriFieldType extends AbstractEcsvFieldType {
  public UriFieldType() {
    super(DataTypes.ANY_URI);

  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      try {
        return new URI(text);
      } catch (final URISyntaxException e) {
        throw new IllegalArgumentException(text + " is not a valid URI", e);
      }
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    StringFieldType.writeQuotedString(out, value);
  }
}
