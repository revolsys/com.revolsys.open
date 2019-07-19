package com.revolsys.ui.html.serializer.type;

import java.io.IOException;

import com.revolsys.record.io.format.xml.XmlWriter;

/**
 * The ObjectSerializer interface defines a method to serailize an object of a
 * known type to an {@link com.revolsys.io.xml.XmlWriter}.
 *
 * @author Paul Austin
 */
public interface TypeSerializer {
  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  void serialize(XmlWriter out, Object value);
}
