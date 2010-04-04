package com.revolsys.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.json.JsonParser.EventType;

public final class JsonParserUtil {
  public static List<Object> getArray(
    final JsonParser parser) {
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

  public static Map<String, Object> getMap(
    final JsonParser parser) {
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

  public static String getString(
    final JsonParser parser) {
    if (parser.getEvent() == EventType.string || parser.hasNext()
      && parser.next() == EventType.string) {
      return parser.getValue();
    } else {
      throw new IllegalStateException("Expecting a string");
    }
  }

  public static Object getValue(
    final JsonParser parser) {
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

  private JsonParserUtil() {
  }
}
