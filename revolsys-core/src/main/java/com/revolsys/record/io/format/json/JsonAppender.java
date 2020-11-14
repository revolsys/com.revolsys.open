package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.map.MapSerializer;

public class JsonAppender {
  public static void appendCharacters(final Appendable appendable, final CharSequence string)
    throws IOException {
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      switch (c) {
        case '"':
          appendable.append("\\\"");
        break;
        case '\\':
          appendable.append("\\\\");
        break;
        case '\b':
          appendable.append("\\b");
        break;
        case '\f':
          appendable.append("\\f");
        break;
        case '\n':
          appendable.append("\\n");
        break;
        case '\r':
          appendable.append("\\r");
        break;
        case '\t':
          appendable.append("\\t");
        break;
        default:
          appendable.append(c);
        break;
      }
    }
  }

  public static void appendList(final Appendable appendable,
    final Collection<? extends Object> values) throws IOException {
    appendable.append('[');
    boolean first = true;
    for (final Object value : values) {
      if (first) {
        first = false;
      } else {
        appendable.append(',');
      }
      JsonAppender.appendValue(appendable, value);
    }
    appendable.append(']');
  }

  public static <K, V> void appendMap(final Appendable appendable, final Map<K, V> map)
    throws IOException {
    appendable.append('{');
    boolean first = true;
    for (final K key : map.keySet()) {
      if (first) {
        first = false;
      } else {
        appendable.append(',');
      }
      final V value = map.get(key);
      appendable.append('"');
      JsonAppender.appendCharacters(appendable, key.toString());
      appendable.append("\":");
      JsonAppender.appendValue(appendable, value);
    }
    appendable.append('}');
  }

  public static void appendText(final Appendable appendable, final Object value) {
    try {
      final String string = DataTypes.toString(value);
      if (string == null) {
        appendable.append("null");
      } else {
        appendable.append('"');
      }
      appendValue(appendable, string);
      appendable.append('"');
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static void appendValue(final Appendable appendable, final Object value) {
    try {
      if (value == null) {
        appendable.append("null");
      } else if (value instanceof Boolean) {
        if ((Boolean)value) {
          appendable.append("true");
        } else {
          appendable.append("false");
        }
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        final double doubleValue = number.doubleValue();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
          appendable.append("null");
        } else {
          appendable.append(Doubles.toString(doubleValue));
        }
      } else if (value instanceof JsonType) {
        final JsonType jsonType = (JsonType)value;
        jsonType.appendJson(appendable);
      } else if (value instanceof Jsonable) {
        final JsonType json = ((MapSerializer)value).asJson();
        if (json != null) {
          json.appendJson(appendable);
        }
      } else if (value instanceof Collection) {
        final Collection<? extends Object> list = (Collection<? extends Object>)value;
        appendList(appendable, list);
      } else if (value instanceof Map) {
        final Map<Object, Object> map = (Map<Object, Object>)value;
        appendMap(appendable, map);
      } else if (value instanceof CharSequence) {
        final CharSequence string = (CharSequence)value;
        appendable.append('"');
        appendCharacters(appendable, string);
        appendable.append('"');
      } else if (value.getClass().isArray()) {
        final List<? extends Object> list = Lists.arrayToList(value);
        appendList(appendable, list);
      } else {
        appendText(appendable, value);
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
