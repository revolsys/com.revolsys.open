package com.revolsys.io.json;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JsonWriterUtil {
  public static void charSequence(
    final PrintWriter out,
    final CharSequence string) {
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      switch (c) {
        case '"':
          out.print("\\\"");
        break;
        case '\\':
          out.print("\\\\");
        break;
        case '/':
          out.print("\\/");
        break;
        case '\b':
          out.print("\\b");
        break;
        case '\f':
          out.print("\\f");
        break;
        case '\n':
          out.print("\\n");
        break;
        case '\r':
          out.print("\\r");
        break;
        case '\t':
          out.print("\\t");
        break;
        default:
          out.print(c);
        break;
      }
    }
  }

  public static void endAttribute(final PrintWriter out) {
    out.print(",\n");
  }

  public static void endList(final PrintWriter out) {
    out.print("]\n");
  }

  public static void endObject(final PrintWriter out) {
    out.print("\n}\n");
  }

  public static void label(final PrintWriter out, final String key) {
    write(out, key);
    out.print(":");
  }

  public static void startList(final PrintWriter out) {
    out.print("[\n");
  }

  public static void startObject(final PrintWriter out) {
    out.print("{\n");
  }

  public static void write(
    final PrintWriter out,
    final List<? extends Object> values) {
    startList(out);
    if (values != null) {
      int i = 0;
      final int size = values.size();
      final Iterator<? extends Object> iterator = values.iterator();
      while (i < size - 1) {
        final Object value = iterator.next();
        write(out, value);
        endAttribute(out);
        i++;
      }
      if (iterator.hasNext()) {
        final Object value = iterator.next();
        write(out, value);
        out.print("\n");
      }
    }
    endList(out);
  }

  public static void write(
    final PrintWriter out,
    final Map<String, ? extends Object> values) {

    startObject(out);
    if (values != null) {
      final Set<String> fields = values.keySet();
      int i = 0;
      final int size = fields.size();
      final Iterator<String> iterator = fields.iterator();
      while (i < size - 1) {
        final String key = iterator.next();
        final Object value = values.get(key);
        label(out, key);
        write(out, value);
        endAttribute(out);
        i++;
      }
      if (iterator.hasNext()) {
        final String key = iterator.next();
        final Object value = values.get(key);
        label(out, key);
        write(out, value);
      }
    }
    endObject(out);
  }

  @SuppressWarnings("unchecked")
  public static void write(final PrintWriter out, final Object value) {
    if (value == null) {
      out.print("null");
    } else if (value instanceof Boolean) {
      out.print(value);
    } else if (value instanceof Number) {
      out.print(value);
    } else if (value instanceof List) {
      final List<? extends Object> list = (List<? extends Object>)value;
      write(out, list);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(out, map);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      out.write('"');
      charSequence(out, string);
      out.write('"');
    } else {
      write(out, value.toString());
    }

  }

  private JsonWriterUtil() {
  }

}
