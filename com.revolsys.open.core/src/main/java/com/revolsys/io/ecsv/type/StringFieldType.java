package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.ecsv.EcsvConstants;

public class StringFieldType extends AbstractEcsvFieldType {

  /**
   * Write a string wrapped in {@link EcsvConstants#DOUBLE_QUOTE} characters.
   * Any {@link EcsvConstants#DOUBLE_QUOTE} characters in the value will be
   * replaced by {@link EcsvConstants#DOUBLE_QUOTE_ESCAPE}.
   * 
   * @param out
   * @param value The value to write.
   * @see #writeString(PrintWriter, Object)
   */
  public static void writeQuotedString(final PrintWriter out, final Object value) {
    if (value != null) {
      out.print(DOUBLE_QUOTE);
      writeString(out, value);
      out.print(DOUBLE_QUOTE);
    }
  }

  /**
   * Write a string replacing any {@link EcsvConstants#DOUBLE_QUOTE} characters
   * in the value will be replaced by {@link EcsvConstants#DOUBLE_QUOTE_ESCAPE}.
   * 
   * @param out
   * @param value The value to write.
   * @see #writeQuotedString(PrintWriter, Object)
   */
  public static void writeString(final PrintWriter out, final Object value) {
    if (value != null) {
      for (final char c : value.toString().toCharArray()) {
        if (c == '"') {
          out.write(DOUBLE_QUOTE_ESCAPE);
        } else {
          out.write(c);
        }
      }
    }
  }

  public StringFieldType() {
    super(DataTypes.STRING);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return new String(text);
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    writeQuotedString(out, value);
  }

}
