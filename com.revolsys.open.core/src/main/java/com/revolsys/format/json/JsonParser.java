package com.revolsys.format.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

public class JsonParser implements Iterator<JsonParser.EventType>, AutoCloseable {
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
      throw new IllegalStateException("Exepecting start array, not:" + parser.getEvent());
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
    } else if (parser.getEvent() == EventType.nullValue) {
      return null;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + parser.getEvent());
    }
  }

  public static Map<String, Object> getMap(final InputStream in) {
    if (in == null) {
      return null;
    } else {
      try (
        final JsonParser parser = new JsonParser(in)) {
        if (parser.next() == EventType.startDocument) {
          return getMap(parser);
        } else {
          return Collections.emptyMap();
        }
      }
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
                  throw new IllegalStateException("Exepecting a value, not:" + value);
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
      throw new IllegalStateException("Exepecting end object, not:" + parser.getEvent());
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
    return (V)read(FileUtil.createUtf8Reader(in));
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final Object in) {
    Reader reader;
    if (in instanceof Clob) {
      try {
        reader = ((Clob)in).getCharacterStream();
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else if (in instanceof Reader) {
      reader = (Reader)in;
    } else if (in instanceof File) {
      final File file = (File)in;
      reader = FileUtil.getReader(file);
    } else {
      reader = new StringReader(in.toString());
    }
    try {
      return (V)read(reader);
    } finally {
      if (in instanceof Clob) {
        try {
          final Clob clob = (Clob)in;
          clob.free();
        } catch (final SQLException e) {
          throw new RuntimeException("Unable to free clob resources", e);
        }
      }
    }
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

  /** Skip to next attribute in any object.*/
  public static String skipToAttribute(final JsonParser parser) {
    while (parser.hasNext()) {
      final EventType eventType = parser.next();
      if (eventType == EventType.string) {
        final String key = getString(parser);
        if (parser.hasNext() && parser.next() == EventType.colon) {
          return key;
        }
      }
    }
    return null;
  }

  /**
   * Skip through the document until the specified object attribute name is
   * found.
   *
   * @param parser The parser.
   * @param fieldName The name of the attribute to skip through.
   */
  public static void skipToAttribute(final JsonParser parser, final String fieldName) {
    while (parser.hasNext()) {
      final EventType eventType = parser.next();
      if (eventType == EventType.string) {
        final String key = getString(parser);
        if (key.equals(fieldName)) {
          if (parser.hasNext() && parser.next() == EventType.colon) {
            return;
          }
        }
      } else if (eventType == EventType.unknown) {
        return;
      }
    }
  }

  /** Skip to next attribute in the same object.*/
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
    this(FileUtil.createUtf8Reader(in));
  }

  public JsonParser(final Reader reader) {
    this.reader = new BufferedReader(reader);
    try {
      this.currentCharacter = this.reader.read();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonParser(final Resource in) throws IOException {
    this(in.getInputStream());
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.reader);
  }

  public int getDepth() {
    return this.depth;
  }

  public EventType getEvent() {
    return this.currentEvent;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T)this.currentValue;
  }

  @Override
  public boolean hasNext() {
    return this.currentEvent != EventType.endDocument;
  }

  private void moveNext() {
    this.nextValue = null;
    try {
      skipWhitespace();
      switch (this.currentCharacter) {
        case ',':
          this.nextEvent = EventType.comma;
          this.currentCharacter = this.reader.read();
        break;
        case ':':
          this.nextEvent = EventType.colon;
          this.currentCharacter = this.reader.read();
        break;
        case '{':
          this.nextEvent = EventType.startObject;
          this.currentCharacter = this.reader.read();
          this.depth++;
        break;
        case '}':
          this.nextEvent = EventType.endObject;
          this.currentCharacter = this.reader.read();
          this.depth--;
        break;
        case '[':
          this.nextEvent = EventType.startArray;
          this.currentCharacter = this.reader.read();
        break;
        case ']':
          this.nextEvent = EventType.endArray;
          this.currentCharacter = this.reader.read();
        break;
        case 't':
          for (int i = 0; i < 3; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.booleanValue;
          this.nextValue = Boolean.TRUE;
          this.currentCharacter = this.reader.read();
        break;
        case 'f':
          for (int i = 0; i < 4; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.booleanValue;
          this.nextValue = Boolean.FALSE;
          this.currentCharacter = this.reader.read();
        break;
        case 'n':
          for (int i = 0; i < 3; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.nullValue;
          this.nextValue = null;
          this.currentCharacter = this.reader.read();
        break;
        case '"':
          this.nextEvent = EventType.string;

          processString();
          this.currentCharacter = this.reader.read();
        break;
        case '-':
          this.nextEvent = EventType.number;

          processNumber();
        break;
        case -1:
          this.nextEvent = EventType.endDocument;
        break;
        default:
          if (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
            this.nextEvent = EventType.number;
            processNumber();
          } else {
            this.nextEvent = EventType.unknown;
          }
        break;
      }
    } catch (final IOException e) {
      this.nextEvent = EventType.endDocument;
    }
  }

  @Override
  public EventType next() {
    if (hasNext()) {
      this.currentValue = this.nextValue;
      this.currentEvent = this.nextEvent;
      moveNext();
      return this.currentEvent;
    } else {
      throw new NoSuchElementException();
    }
  }

  private void processNumber() throws IOException {
    final StringBuilder text = new StringBuilder();
    if (this.currentCharacter == '-') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
    }
    while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
    }
    if (this.currentCharacter == '.') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
      while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
    }

    if (this.currentCharacter == 'e' || this.currentCharacter == 'E') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
      if (this.currentCharacter == '-' || this.currentCharacter == '+') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
      while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
    }
    this.nextValue = new BigDecimal(text.toString());
  }

  private void processString() throws IOException {
    final StringBuilder text = new StringBuilder();
    this.currentCharacter = this.reader.read();
    while (this.currentCharacter != '"') {
      if (this.currentCharacter == '\\') {
        this.currentCharacter = this.reader.read();
        switch (this.currentCharacter) {
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
            text.append((char)this.currentCharacter);
          break;
        }
      } else {
        text.append((char)this.currentCharacter);
      }
      this.currentCharacter = this.reader.read();
    }
    this.nextValue = text.toString();
  }

  @Override
  public void remove() {
  }

  private void skipWhitespace() throws IOException {
    while (Character.isWhitespace(this.currentCharacter)) {
      this.currentCharacter = this.reader.read();
    }
  }

  @Override
  public String toString() {
    return this.currentEvent + " : " + this.currentValue;
  }
}
