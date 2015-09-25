package com.revolsys.ui.html.serializer.type;

import com.revolsys.record.io.format.xml.XmlWriter;

/**
 * Serialize a date with just the date fields.
 *
 * @author Paul Austin
 */
public class BooleanSerializer implements TypeSerializer {
  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   */
  @Override
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
