package com.revolsys.format.xml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.input.XmlStreamReader;
import org.springframework.core.io.Resource;

import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class XmlReader implements XMLStreamConstants, AutoCloseable {
  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  private XMLStreamReader parser;

  private int depth = 0;

  public XmlReader(final Resource resource) {
    final InputStream inputStream = SpringUtil.getInputStream(resource);
    try {
      final XmlStreamReader reader = new XmlStreamReader(inputStream);
      this.parser = FACTORY.createXMLStreamReader(reader);
      skipToStartElement();
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close() {
    try {
      this.parser.close();
    } catch (final XMLStreamException e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  public int getDepth() {
    return this.depth;
  }

  public String getElementText() {
    final StringBuilder text = new StringBuilder();
    if (getEventType() == XMLStreamConstants.START_ELEMENT) {
      final int depth = this.depth;
      while (next() != XMLStreamConstants.END_ELEMENT && getDepth() >= depth) {
        switch (this.parser.getEventType()) {
          case XMLStreamConstants.CHARACTERS:
            text.append(this.parser.getText());
          break;
        }
      }
    }
    return text.toString();
  }

  public double getElementTextDouble(final double defaultValue) {
    final String text = getElementText();
    if (Property.hasValue(text)) {
      try {
        return Double.parseDouble(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public int getElementTextInt(final int defaultValue) {
    final String text = getElementText();
    if (Property.hasValue(text)) {
      try {
        return Integer.parseInt(text);
      } catch (final Throwable e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public int getEventType() {
    return this.parser.getEventType();
  }

  public String getLocalPart() {
    return getName().getLocalPart();
  }

  public QName getName() {
    return this.parser.getName();
  }

  public XMLStreamReader getParser() {
    return this.parser;
  }

  public int next() {
    try {
      final int next = this.parser.next();
      switch (next) {
        case XMLStreamConstants.START_ELEMENT:
          this.depth++;
        break;
        case XMLStreamConstants.END_ELEMENT:
          this.depth--;
        break;

        default:
        break;
      }
      return next;
    } catch (final XMLStreamException e) {
      return (Integer)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public boolean skipToStartElement() {
    while (next() != XMLStreamConstants.START_ELEMENT) {
      if (getEventType() == XMLStreamConstants.END_DOCUMENT) {
        return false;
      }
    }
    return true;
  }

  public boolean skipToStartElement(final int depth, final QName elementName) {
    while (this.depth >= depth) {
      next();
      if (this.depth < depth) {
        return false;
      } else if (getEventType() == END_DOCUMENT) {
        return false;
      } else if (getEventType() == START_ELEMENT) {
        final QName currentElement = getName();
        if (currentElement.equals(elementName)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean skipToStartElements(final int depth, final Collection<QName> elementNames) {
    try {
      while (this.depth >= depth) {
        next();
        if (this.depth < depth) {
          return false;
        } else if (getEventType() == END_DOCUMENT) {
          return false;
        } else if (getEventType() == START_ELEMENT) {
          final QName elementName = getName();
          if (elementNames.contains(elementName)) {
            return true;
          }
        }
      }
    } catch (final NoSuchElementException e) {
    }
    return false;
  }

  public boolean skipToStartElements(final int depth, final QName... elementNames) {
    return skipToStartElements(depth, Arrays.asList(elementNames));
  }

  @Override
  public String toString() {
    final int eventType = getEventType();
    switch (eventType) {
      case XMLStreamConstants.START_ELEMENT:
        return "<" + getLocalPart() + ">";
      case XMLStreamConstants.END_ELEMENT:
        return "</" + getLocalPart() + ">";
      case XMLStreamConstants.END_DOCUMENT:
        return "EOF";
      default:
        return getElementText();
    }
  }
}
