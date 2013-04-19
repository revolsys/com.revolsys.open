package com.revolsys.io.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.util.MathUtil;

public class JsonParser implements Iterator<JsonParser.EventType> {
  public enum EventType {
    booleanValue, colon, comma, endArray, endDocument, endObject, nullValue, number, startArray, startDocument, startObject, string, unknown
  }

  public static List<Object> getArray(final JsonParser parser) {
    if (parser.getEvent() == EventType.startArray || parser.hasNext()
      && parser.next() == EventType.startArray) {
      EventType event = parser.getEvent();
      final List<Object> list = new ArrayList<Object>();
      do {
        final Object value = getValue(parser);
        if (value instanceof EventType) {
          event = (EventType)value;

        } else {
          list.add(value);
          event = parser.next();
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not:" + event);
      }
      return list;
    } else {
      throw new IllegalStateException("Exepecting start array, not:"
        + parser.getEvent());
    }

  }

  public static double[] getDoubleArray(final JsonParser parser) {
    if (parser.getEvent() == EventType.startArray || parser.hasNext()
      && parser.next() == EventType.startArray) {
      EventType event = parser.getEvent();
      final List<Number> list = new ArrayList<Number>();
      do {
        final Object value = getValue(parser);
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          list.add((Number)value);
          event = parser.next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return MathUtil.toDoubleArray(list);
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + parser.getEvent());
    }
  }

  public static Map<String, Object> getMap(final InputStream in) {
    final JsonParser parser = new JsonParser(in);
    try {
      if (parser.next() == EventType.startDocument) {
        return getMap(parser);
      } else {
        return Collections.emptyMap();
      }
    } finally {
      parser.close();
    }
  }

  public static Map<String, Object> getMap(final JsonParser parser) {
    if (parser.getEvent() == EventType.startObject || parser.hasNext()
      && parser.next() == EventType.startObject) {
      EventType event = parser.getEvent();
      final Map<String, Object> map = new LinkedHashMap<String, Object>();
      do {
        if (parser.hasNext() && parser.next() == EventType.string) {
          final String key = getString(parser);
          if (parser.hasNext()) {
            if (parser.next() == EventType.colon) {
              if (parser.hasNext()) {
                final Object value = getValue(parser);
                if (value instanceof EventType) {
                  throw new IllegalStateException("Exepecting a value, not:"
                    + value);
                }
                map.put(key, value);
              }
            }
          }
          event = parser.next();
        } else {
          event = parser.getEvent();
        }
      } while (event == EventType.comma);
      if (event != EventType.endObject) {
        throw new IllegalStateException("Exepecting end object, not:" + event);
      }
      return map;
    } else {
      throw new IllegalStateException("Exepecting end object, not:"
        + parser.getEvent());
    }

  }

  public static Map<String, Object> getMap(final Reader reader) {
    final JsonParser parser = new JsonParser(reader);
    try {
      if (parser.next() == EventType.startDocument) {
        return getMap(parser);
      } else {
        return Collections.emptyMap();
      }
    } finally {
      parser.close();
    }
  }

  public static String getString(final JsonParser parser) {
    if (parser.getEvent() == EventType.string || parser.hasNext()
      && parser.next() == EventType.string) {
      return parser.getValue();
    } else {
      throw new IllegalStateException("Expecting a string");
    }
  }

  public static Object getValue(final JsonParser parser) {
    // TODO empty array
    if (parser.hasNext()) {
      final EventType event = parser.next();
      if (event == EventType.startArray) {
        return getArray(parser);
      } else if (event == EventType.startObject) {
        return getMap(parser);
      } else if (event == EventType.booleanValue) {
        return parser.getValue();
      } else if (event == EventType.nullValue) {
        return parser.getValue();
      } else if (event == EventType.string) {
        return parser.getValue();
      } else if (event == EventType.number) {
        return parser.getValue();
      } else {
        return event;
      }
    } else {
      throw new IllegalStateException("Expecting a value not EOF");
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final InputStream in) {
    return (V)read(new InputStreamReader(in));
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final Reader in) {
    final JsonParser parser = new JsonParser(in);
    try {
      if (parser.hasNext()) {
        final EventType event = parser.next();
        if (event == EventType.startDocument) {
          return (V)getValue(parser);
        }
      }
      return null;
    } finally {
      parser.close();
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final String in) {
    return (V)read(new StringReader(in));
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final Object in) {
    Reader reader;
    if (in instanceof Clob) {
      try {
        reader = ((Clob)in).getCharacterStream();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else if (in instanceof Reader) {
      reader = (Reader)in;
    } else {
      reader = new StringReader(in.toString());
    }
    return (V)read(reader);
  }

  /**
   * Skip through the document until the specified object attribute name is
   * found.
   * 
   * @param parser The parser.
   * @param attributeName The name of the attribute to skip through.
   */
  public static void skipToAttribute(final JsonParser parser,
    final String attributeName) {
    while (parser.hasNext()) {
      final EventType eventType = parser.next();
      if (eventType == EventType.string) {
        final String key = getString(parser);
        if (key.equals(attributeName)) {
          if (parser.hasNext() && parser.next() == EventType.colon) {
            return;
          }
        }
      } else if (eventType == EventType.unknown) {
        return;
      }
    }
  }

  public static String skipToNextAttribute(final JsonParser parser) {
    int objectCount = 0;
    while (parser.hasNext()) {
      final EventType eventType = parser.next();
      if (objectCount == 0 && eventType == EventType.string) {
        final String key = getString(parser);
        if (parser.hasNext() && parser.next() == EventType.colon) {
          return key;
        }
      } else if (eventType == EventType.startObject) {
        objectCount++;
      } else if (eventType == EventType.endObject) {
        if (objectCount == 0) {
          return null;
        } else {
          objectCount--;
        }
      }
    }
    return null;
  }

  private int currentCharacter;

  private EventType currentEvent = EventType.startDocument;

  private Object currentValue;

  private int depth;

  private EventType nextEvent = EventType.startDocument;

  private Object nextValue;

  private final Reader reader;

  public JsonParser(final InputStream in) {
    this(new InputStreamReader(in));
  }

  public JsonParser(final Reader reader) {
    this.reader = new BufferedReader(reader);
    try {
      currentCharacter = this.reader.read();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonParser(final Resource in) throws IOException {
    this(in.getInputStream());
  }

  public void close() {
    FileUtil.closeSilent(reader);
  }

  public int getDepth() {
    return depth;
  }

  public EventType getEvent() {
    return currentEvent;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T)currentValue;
  }

  @Override
  public boolean hasNext() {
    return currentEvent != EventType.endDocument;
  }

  private void moveNext() {
    nextValue = null;
    try {
      skipWhitespace();
      switch (currentCharacter) {
        case ',':
          nextEvent = EventType.comma;
          currentCharacter = reader.read();
        break;
        case ':':
          nextEvent = EventType.colon;
          currentCharacter = reader.read();
        break;
        case '{':
          nextEvent = EventType.startObject;
          currentCharacter = reader.read();
          depth++;
        break;
        case '}':
          nextEvent = EventType.endObject;
          currentCharacter = reader.read();
          depth--;
        break;
        case '[':
          nextEvent = EventType.startArray;
          currentCharacter = reader.read();
        break;
        case ']':
          nextEvent = EventType.endArray;
          currentCharacter = reader.read();
        break;
        case 't':
          for (int i = 0; i < 3; i++) {
            currentCharacter = reader.read();
          }
          nextEvent = EventType.booleanValue;
          nextValue = Boolean.TRUE;
          currentCharacter = reader.read();
        break;
        case 'f':
          for (int i = 0; i < 4; i++) {
            currentCharacter = reader.read();
          }
          nextEvent = EventType.booleanValue;
          nextValue = Boolean.FALSE;
          currentCharacter = reader.read();
        break;
        case 'n':
          for (int i = 0; i < 3; i++) {
            currentCharacter = reader.read();
          }
          nextEvent = EventType.nullValue;
          nextValue = null;
          currentCharacter = reader.read();
        break;
        case '"':
          nextEvent = EventType.string;

          processString();
          currentCharacter = reader.read();
        break;
        case '-':
          nextEvent = EventType.number;

          processNumber();
        break;
        case -1:
          nextEvent = EventType.endDocument;
        break;
        default:
          if (currentCharacter >= '0' && currentCharacter <= '9') {
            nextEvent = EventType.number;
            processNumber();
          } else {
            nextEvent = EventType.unknown;
          }
        break;
      }
    } catch (final IOException e) {
      nextEvent = EventType.endDocument;
    }
  }

  @Override
  public EventType next() {
    if (hasNext()) {
      currentValue = nextValue;
      currentEvent = nextEvent;
      moveNext();
      return currentEvent;
    } else {
      throw new NoSuchElementException();
    }
  }

  private void processNumber() throws IOException {
    final StringBuffer text = new StringBuffer();
    if (currentCharacter == '-') {
      text.append((char)currentCharacter);
      currentCharacter = reader.read();
    }
    while (currentCharacter >= '0' && currentCharacter <= '9') {
      text.append((char)currentCharacter);
      currentCharacter = reader.read();
    }
    if (currentCharacter == '.') {
      text.append((char)currentCharacter);
      currentCharacter = reader.read();
      while (currentCharacter >= '0' && currentCharacter <= '9') {
        text.append((char)currentCharacter);
        currentCharacter = reader.read();
      }
    }

    if (currentCharacter == 'e' || currentCharacter == 'E') {
      text.append((char)currentCharacter);
      currentCharacter = reader.read();
      if (currentCharacter == '-' || currentCharacter == '+') {
        text.append((char)currentCharacter);
        currentCharacter = reader.read();
      }
      while (currentCharacter >= '0' && currentCharacter <= '9') {
        text.append((char)currentCharacter);
        currentCharacter = reader.read();
      }
    }
    nextValue = new BigDecimal(text.toString());
  }

  private void processString() throws IOException {
    final StringBuffer text = new StringBuffer();
    currentCharacter = reader.read();
    while (currentCharacter != '"') {
      if (currentCharacter == '\\') {
        currentCharacter = reader.read();
        switch (currentCharacter) {
          case 'n':
            text.append('\n');
          break;
          case 'r':
            text.append('\r');
          break;
          case 't':
            text.append('\t');
          break;
          case 'b':
            text.setLength(text.length() - 1);
          break;
          case 'u':
          // TODO process hex
          break;
          default:
            text.append((char)currentCharacter);
          break;
        }
      } else {
        text.append((char)currentCharacter);
      }
      currentCharacter = reader.read();
    }
    nextValue = text.toString();
  }

  @Override
  public void remove() {
  }

  private void skipWhitespace() throws IOException {
    while (Character.isWhitespace(currentCharacter)) {
      currentCharacter = reader.read();
    }
  }

  @Override
  public String toString() {
    return currentEvent + " : " + currentValue;
  }
}
