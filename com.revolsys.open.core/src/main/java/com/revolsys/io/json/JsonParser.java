package com.revolsys.io.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;

public class JsonParser implements Iterator<JsonParser.EventType> {
  public enum EventType {
    booleanValue, colon, comma, endArray, endDocument, endObject, nullValue, number, startArray, startDocument, startObject, string, unknown
  }

  private int currentCharacter;

  private EventType currentEvent = EventType.startDocument;

  private Object currentValue;

  private int depth;

  private EventType nextEvent = EventType.startDocument;

  private Object nextValue;

  private final Reader reader;

  public JsonParser(
    final InputStream in)
    throws IOException {
    this(new InputStreamReader(in));
  }

  public JsonParser(
    final Reader reader)
    throws IOException {
    this.reader = new BufferedReader(reader);
    currentCharacter = this.reader.read();
  }

  public JsonParser(
    final Resource in)
    throws IOException {
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

  private void processNumber()
    throws IOException {
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

  private void processString()
    throws IOException {
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

  public void remove() {
  }

  private void skipWhitespace()
    throws IOException {
    while (Character.isWhitespace(currentCharacter)) {
      currentCharacter = reader.read();
    }
  }

  @Override
  public String toString() {
    return currentEvent + " : " + currentValue;
  }
}
