package com.revolsys.ui.html.serializer.type;

import java.io.IOException;
import java.util.Locale;

import com.revolsys.xml.io.XmlWriter;

/**
 * The ObjectSerializer interface defines a method to serailize an object of a
 * known type to an {@link com.revolsys.xml.io.XmlWriter}.
 * 
 * @author Paul Austin
 */
public interface TypeSerializer {
  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  void serialize(XmlWriter out, Object value, Locale locale);
}
