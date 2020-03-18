package com.revolsys.ui.html.serializer.key;

import java.io.IOException;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.io.format.xml.XmlWriter;

/**
 * The ObjectSerializer interface defines a method to serailize an object to an
 * {@link com.revolsys.io.xml.XmlWriter}.
 *
 * @author Paul Austin
 */
public interface KeySerializer extends ObjectWithProperties {
  String getKey();

  String getLabel();

  String getName();

  String getSortFieldName();

  String getWidth();

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  void serialize(XmlWriter out, Object object);

  KeySerializer setKey(String userGroupName);

  KeySerializer setLabel(String title);

  String toString(Object object);
}
