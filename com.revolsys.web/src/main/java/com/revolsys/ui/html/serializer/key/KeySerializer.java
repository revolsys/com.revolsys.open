package com.revolsys.ui.html.serializer.key;

import java.io.IOException;
import java.util.Locale;

import com.revolsys.xml.io.XmlWriter;

/**
 * The ObjectSerializer interface defines a method to serailize an object to an
 * {@link com.revolsys.xml.io.XmlWriter}.
 * 
 * @author Paul Austin
 */
public interface KeySerializer {
  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  void serialize(XmlWriter out, Object value, String key, Locale locale) ;
}
