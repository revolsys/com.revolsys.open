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
package com.revolsys.format.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.input.XmlStreamReader;
import org.springframework.core.io.Resource;

import com.revolsys.spring.SpringUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

/**
 * The StaxUtils class provides utility methods for processing XML using the
 * {@link XMLStreamReader} class.
 *
 * @author Paul Austin
 */
public final class StaxUtils {
  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  public static void closeSilent(final XMLStreamReader in) {
    if (in != null) {
      try {
        in.close();
      } catch (final XMLStreamException e) {
      }
    }
  }

  public static XMLStreamReader createXmlReader(final InputStream inputStream) {
    try {
      final XmlStreamReader reader = new XmlStreamReader(inputStream);
      return StaxUtils.FACTORY.createXMLStreamReader(reader);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  public static XMLStreamReader createXmlReader(final Reader reader) {
    try {
      return StaxUtils.FACTORY.createXMLStreamReader(reader);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  public static XMLStreamReader createXmlReader(final Resource resource) {
    final InputStream inputStream = SpringUtil.getInputStream(resource);
    return createXmlReader(inputStream);
  }

  public static String getAttribute(final XMLStreamReader parser, final QName typePath) {
    final String value = parser.getAttributeValue(typePath.getNamespaceURI(),
      typePath.getLocalPart());
    return value;
  }

  public static boolean getBooleanAttribute(final XMLStreamReader parser, final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Boolean.parseBoolean(value);
    } else {
      return false;
    }
  }

  public static double getDoubleAttribute(final XMLStreamReader parser, final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Double.parseDouble(value);
    } else {
      return Double.NaN;
    }
  }

  public static String getElementText(final XMLStreamReader parser) {
    final StringBuilder text = new StringBuilder();
    require(parser, XMLStreamConstants.START_ELEMENT, null, null);
    while (next(parser) != XMLStreamConstants.END_ELEMENT) {
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

  public static double getElementTextDouble(final XMLStreamReader in, final double defaultValue) {
    final String text = getElementText(in);
    if (Property.hasValue(text)) {
      try {
        return Double.parseDouble(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public static int getElementTextInt(final XMLStreamReader in, final int defaultValue) {
    final String text = getElementText(in);
    if (Property.hasValue(text)) {
      try {
        return Integer.parseInt(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public static int getIntAttribute(final XMLStreamReader parser, final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return 0;
    }
  }

  public static long getLongAttribute(final XMLStreamReader parser, final String namespaceUri,
    final String name) {
    final String value = parser.getAttributeValue(namespaceUri, name);
    if (value != null) {
      return Long.parseLong(value);
    } else {
      return 0;
    }
  }

  public static QName getQNameAttribute(final XMLStreamReader in, final QName fieldName) {
    final String value = StaxUtils.getAttribute(in, fieldName);
    final NamespaceContext namespaceContext = in.getNamespaceContext();
    final QName qName = getXmlQName(namespaceContext, value);
    return qName;
  }

  public static QName getXmlQName(final NamespaceContext context, final String value) {
    if (value == null) {
      return null;
    } else {
      final int colonIndex = value.indexOf(':');
      if (colonIndex == -1) {
        return new QName(value);
      } else {
        final String prefix = value.substring(0, colonIndex);
        final String name = value.substring(colonIndex + 1);
        final String namespaceUri = context.getNamespaceURI(prefix);
        return new QName(namespaceUri, name, prefix);
      }
    }
  }

  public static boolean isEndElementLocalName(final XMLStreamReader parser, final QName name) {
    if (parser.isEndElement()) {
      final QName elementName = parser.getName();
      if (elementName.getLocalPart().equals(name.getLocalPart())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static boolean matchElementLocalName(final XMLStreamReader parser, final QName element) {
    final String currentLocalName = parser.getLocalName();
    final String elementLocalName = element.getLocalPart();
    return currentLocalName.equals(elementLocalName);
  }

  public static int next(final XMLStreamReader parser) {
    try {
      return parser.next();
    } catch (final XMLStreamException e) {
      return (Integer)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public static int nextTag(final XMLStreamReader parser) {
    try {
      return parser.nextTag();
    } catch (final XMLStreamException e) {
      return (Integer)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public static void require(final XMLStreamReader parser, final int type,
    final String namespaceURI, final String localPart) {
    try {
      parser.require(type, namespaceURI, localPart);
    } catch (final XMLStreamException e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void require(final XMLStreamReader parser, final QName element) {
    require(parser, XMLStreamConstants.START_ELEMENT, element.getNamespaceURI(),
      element.getLocalPart());
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void requireLocalName(final XMLStreamReader parser, final QName element) {
    require(parser, XMLStreamConstants.START_ELEMENT, null, element.getLocalPart());
  }

  /**
   * Check that the parser is currently at the specified XML element.
   *
   * @param parser The STAX XML parser.
   * @param element The expected XML element.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void requireLocalPart(final XMLStreamReader parser, final QName element) {
    require(parser, XMLStreamConstants.START_ELEMENT, null, element.getLocalPart());
  }

  /**
   * Skip all elements and content until the end of the current element.
   *
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void skipSubTree(final XMLStreamReader parser) {
    require(parser, XMLStreamConstants.START_ELEMENT, null, null);
    int level = 1;
    while (level > 0) {
      final int eventType = next(parser);
      if (eventType == XMLStreamConstants.END_ELEMENT) {
        --level;
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        ++level;
      }
    }
  }

  /**
   * Skip all events until the next start element which is a child of the
   * current element has one of the elementNames.
   *
   * @param parser The STAX XML parser.
   * @param elementNames The names of the elements to find
   * @return True if one of the elements was found.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static boolean skipToChildStartElements(final XMLStreamReader parser,
    final Collection<QName> elementNames) {
    int count = 0;
    QName elementName = null;
    if (parser.isEndElement()) {
      elementName = parser.getName();
      if (elementNames.contains(elementName)) {
        nextTag(parser);
      } else {
        return false;
      }
    }
    if (parser.isStartElement()) {
      elementName = parser.getName();
      if (elementNames.contains(elementName)) {
        return true;
      }
    }
    do {
      while (next(parser) != XMLStreamConstants.START_ELEMENT) {
        if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
          return false;
        } else if (parser.isEndElement()) {
          if (count == 0) {
            return false;
          }
          count--;
        }
      }
      count++;
      elementName = parser.getName();
    } while (!elementNames.contains(elementName));
    return true;
  }

  /**
   * Skip all events until the next start element which is a child of the
   * current element has one of the elementNames.
   *
   * @param parser The STAX XML parser.
   * @param elementNames The names of the elements to find
   * @return True if one of the elements was found.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static boolean skipToChildStartElements(final XMLStreamReader parser,
    final QName... elementNames) {
    final List<QName> names = Arrays.asList(elementNames);
    return skipToChildStartElements(parser, names);
  }

  /**
   * Skip all events until the next end element event.
   *
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipToEndElement(final XMLStreamReader parser) {
    while (next(parser) != XMLStreamConstants.END_ELEMENT) {
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
  public static void skipToEndElement(final XMLStreamReader parser, final QName name) {
    while (!parser.isEndElement() || !parser.getName().equals(name)) {
      next(parser);
    }
    next(parser);
  }

  /**
   * Skip all events until the next end element event.
   *
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static void skipToEndElementByLocalName(final XMLStreamReader parser, final QName name) {
    while (!parser.isEndElement() || !parser.getName().getLocalPart().equals(name.getLocalPart())) {
      next(parser);
      if (parser.getEventType() == XMLStreamConstants.START_ELEMENT
        || parser.getEventType() == XMLStreamConstants.END_ELEMENT) {
      }
    }
    skipWhitespace(parser);
  }

  /**
   * Skip all events until the next start element event.
   *
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipToStartElement(final XMLStreamReader parser) {
    while (next(parser) != XMLStreamConstants.START_ELEMENT) {
      if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return parser.getEventType();
      }
    }
    return parser.getEventType();
  }

  public static boolean skipToStartElement(final XMLStreamReader parser, final String localName) {
    String currentName = null;
    do {
      while (next(parser) != XMLStreamConstants.START_ELEMENT) {
        if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
          return false;
        }
      }
      currentName = parser.getLocalName();
    } while (!currentName.equals(localName));
    return true;
  }

  /**
   * Skip all events until the next start element event which is for an element
   * with one of the elementNames.
   *
   * @param parser The STAX XML parser.
   * @param elementNames The names of the elements to find
   * @return The name of the matching element, null if end of document is
   *         reached.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static boolean skipToStartElements(final XMLStreamReader parser,
    final Collection<QName> elementNames) {
    QName elementName = null;
    do {
      while (next(parser) != XMLStreamConstants.START_ELEMENT) {
        if (parser.getEventType() == XMLStreamConstants.END_DOCUMENT) {
          return false;
        }
      }
      elementName = parser.getName();
    } while (!elementNames.contains(elementName));
    return true;
  }

  public static boolean skipToStartElements(final XMLStreamReader parser,
    final QName... elementNames) {
    return skipToStartElements(parser, Arrays.asList(elementNames));
  }

  public static void skipToStartOrEndElement(final XMLStreamReader parser) {
    require(parser, XMLStreamConstants.END_ELEMENT, null, null);
    while (true) {
      final int eventType = next(parser);
      if (eventType == XMLStreamConstants.END_ELEMENT) {
        return;
      } else if (eventType == XMLStreamConstants.START_ELEMENT) {
        return;
      } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
        return;
      }
    }
  }

  /**
   * Skip any whitespace until an start or end of element is found.
   *
   * @param parser The STAX XML parser.
   * @throws XMLStreamException If an exception processing the XML occurs.
   */
  public static int skipWhitespace(final XMLStreamReader parser) {
    while (next(parser) == XMLStreamConstants.CHARACTERS && parser.isWhiteSpace()) {
      switch (parser.getEventType()) {
        case XMLStreamConstants.END_DOCUMENT:
        case XMLStreamConstants.START_ELEMENT:
          return parser.getEventType();
      }
    }
    return parser.getEventType();
  }

  public static void startElement(final XMLStreamWriter writer, final QName element) {
    try {
      writer.writeStartElement(element.getPrefix(), element.getLocalPart(),
        element.getNamespaceURI());
    } catch (final XMLStreamException e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  public static String toString(final XMLStreamReader in) {
    if (in == null) {
      return null;
    } else {
      try {
        final int eventType = in.getEventType();
        switch (eventType) {
          case XMLStreamConstants.START_DOCUMENT:
            return "startDocument";
          case XMLStreamConstants.END_DOCUMENT:
            return "endDocument";
          case XMLStreamConstants.START_ELEMENT:
            return "start " + in.getName();
          case XMLStreamConstants.END_ELEMENT:
            return "end " + in.getName();
          case XMLStreamConstants.CDATA:
            return "cdata";
          case XMLStreamConstants.CHARACTERS:
            return "characters " + in.getElementText();
          case XMLStreamConstants.SPACE:
            return "whitespace";

          default:
            return "unknown:" + eventType;
        }
      } catch (final XMLStreamException e) {
        return e.getMessage();
      }
    }
  }

  /**
   * Construct a new StaxUtils.
   */
  private StaxUtils() {
  }
}
