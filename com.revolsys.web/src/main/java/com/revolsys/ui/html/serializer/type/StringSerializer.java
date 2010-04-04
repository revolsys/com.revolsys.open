package com.revolsys.ui.html.serializer.type;

import java.io.IOException;
import java.util.Locale;

import com.revolsys.xml.io.XmlWriter;

/**
 * Serialize an object as a string..
 * 
 * @author Paul Austin
 */
public class StringSerializer implements TypeSerializer {
  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(final XmlWriter out, final Object value,
    final Locale locale) throws IOException {
    String stringValue = value.toString();
    if (stringValue.length() == 0) {
      out.text('-');
    } else {
      out.text(stringValue);
    }
  }
}
