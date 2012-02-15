package com.revolsys.ui.html.serializer.type;

import java.text.DateFormat;

import com.revolsys.io.xml.XmlWriter;

/**
 * Serialize a date with just the date fields.
 * 
 * @author Paul Austin
 */
public class BooleanSerializer implements TypeSerializer {
  /** The date format style. */
  private final int dateStyle = DateFormat.DEFAULT;

  /**
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   */
  public void serialize(final XmlWriter out, final Object value) {
    if (value instanceof Boolean) {
      final Boolean bool = (Boolean)value;
      if (bool.booleanValue()) {
        out.text("Yes");
      } else {
        out.text("No");
      }
    } else {
      out.text("No");
    }
  }

}
