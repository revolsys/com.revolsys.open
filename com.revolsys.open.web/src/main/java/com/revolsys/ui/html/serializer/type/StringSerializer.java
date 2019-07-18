package com.revolsys.ui.html.serializer.type;

import java.io.IOException;

import com.revolsys.record.io.format.xml.XmlWriter;

/**
 * Serialize an object as a string..
 *
 * @author Paul Austin
 */
public class StringSerializer implements TypeSerializer {
  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  @Override
  public void serialize(final XmlWriter out, final Object value) {
    final String stringValue = value.toString();
    if (stringValue.length() == 0) {
      out.text('-');
    } else {
      out.text(stringValue);
    }
  }
}
