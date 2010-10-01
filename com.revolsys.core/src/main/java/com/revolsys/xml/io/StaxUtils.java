/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.xml.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * The StaxUtils class provides utility methods for processing XML using the
 * {@link XMLStreamReader} class.
 * 
 * @author Paul Austin
 */
@SuppressWarnings("restriction")
public final class StaxUtils {
  public static double getDoubleAttribute(
    final XMLStreamReader parser,
    final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Double.parseDouble(value);
    } else {
      return Double.NaN;
    }
  }

  public static String getElementText(
    final XMLStreamReader parser)
    throws XMLStreamException {
    final StringBuffer text = new StringBuffer();
    parser.require(XMLStreamConstants.START_ELEMENT, null, null);
    while (parser.next() != XMLStreamConstants.END_ELEMENT) {
      switch (parser.getEventType()) {
        case XMLStreamConstants.CHARACTERS:
          text.append(parser.getText());
        break;
        case XMLStreamConstants.START_ELEMENT:
          text.append(getElementText(parser));
        break;
      }
    }
    return text.toString();
  }

  public static int getIntAttribute(
    final XMLStreamReader parser,
    final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return 0;
    }
  }

  /**
   * Check that the parser is currently at the specified XML element.
   * 
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static boolean matchElementLocalName(
    final XMLStreamReader parser,
    final QName element)
    throws XMLStreamException {
    final String currentLocalName = parser.getLocalName();
    final String elementLocalName = element.getLocalPart();
    return currentLocalName.equals(elementLocalName);
  }

  /**
   * Check that the parser is currently at the specified XML element.
   * 
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void require(
    final XMLStreamReader parser,
    final QName element)
    throws XMLStreamException {
    parser.require(XMLStreamConstants.START_ELEMENT, element.getNamespaceURI(),
      element.getLocalPart());
  }

  /**
   * Check that the parser is currently at the specified XML element.
   * 
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void requireLocalName(
    final XMLStreamReader parser,
    final QName element)
    throws XMLStreamException {
    parser.require(XMLStreamConstants.START_ELEMENT, null,
      element.getLocalPart());
  }

  /**
   * Skip all elements and content until the end of the current element.
   * 
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void skipSubTree(
    final XMLStreamReader parser)
    throws XMLStreamException {
    parser.require(XMLStreamConstants.START_ELEMENT, null, null);
    int level = 1;
    while (level > 0) {
      final int eventType = parser.next();
      if (eventType == XMLStreamConstants.END_ELEMENT) {
        --level;
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        ++level;
      }
    }
  }

  /**
   * Skip all events until the next end element event.
   * 
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipToEndElement(
    final XMLStreamReader parser)
    throws XMLStreamException {
    while (parser.next() != XMLStreamConstants.END_ELEMENT) {
      if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return parser.getEventType();
      }
    }
    return parser.getEventType();
  }

  /**
   * Skip all events until the next end element event.
   * 
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void skipToEndElement(
    final XMLStreamReader parser,
    final QName name)
    throws XMLStreamException {
    while (!parser.isEndElement() || !parser.getName().equals(name)) {
      parser.next();
    }
  }

  /**
   * Skip all events until the next start element event.
   * 
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipToStartElement(
    final XMLStreamReader parser)
    throws XMLStreamException {
    while (parser.next() != XMLStreamConstants.START_ELEMENT) {
      if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return parser.getEventType();
      }
    }
    return parser.getEventType();
  }

  /**
   * Skip any whitespace until an start or end of element is found.
   * 
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipWhitespace(
    final XMLStreamReader parser)
    throws XMLStreamException {
    while (parser.next() == XMLStreamConstants.CHARACTERS
      && parser.isWhiteSpace()) {
      switch (parser.getEventType()) {
        case XMLStreamConstants.END_DOCUMENT:
        case XMLStreamConstants.START_ELEMENT:
          return parser.getEventType();
      }
    }
    return parser.getEventType();
  }

  public static void startElement(
    final XMLStreamWriter writer,
    final QName element)
    throws XMLStreamException {
    writer.writeStartElement(element.getPrefix(), element.getLocalPart(),
      element.getNamespaceURI());
  }

  /**
   * Construct a new StaxUtils.
   */
  private StaxUtils() {
  }
}
