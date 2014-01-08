package com.revolsys.io.json;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.StringPrinter;
import com.revolsys.util.CollectionUtil;

public final class JsonWriterUtil {
  public static void charSequence(final PrintWriter out,
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

  public static void endAttribute(final PrintWriter out, final String indent) {
    out.print(",");
    newLine(out, indent);
  }

  public static void endList(final PrintWriter out) {
    out.print("]");
  }

  public static void endObject(final PrintWriter out) {
    out.print("}");
  }

  public static void label(final PrintWriter out, final String key,
    final String indent) {
    writeIndent(out, indent);
    write(out, key, null);
    out.print(":");
  }

  public static void newLine(final PrintWriter out, final String indent) {
    if (indent != null) {
      out.print("\n");
    }
  }

  public static void startList(final PrintWriter out, final String indent) {
    out.print("[");
    newLine(out, indent);
  }

  public static void startObject(final PrintWriter out, final String indent) {
    out.print("{");
    newLine(out, indent);
  }

  public static void write(final PrintWriter out,
    final Collection<? extends Object> values, final String indent) {
    startList(out, indent);
    String newIndent = indent;
    if (newIndent != null) {
      newIndent += "  ";
    }
    if (values != null) {
      int i = 0;
      final int size = values.size();
      final Iterator<? extends Object> iterator = values.iterator();
      while (i < size - 1) {
        writeIndent(out, newIndent);
        final Object value = iterator.next();
        write(out, value, newIndent);
        endAttribute(out, indent);
        i++;
      }
      if (iterator.hasNext()) {
        writeIndent(out, newIndent);
        final Object value = iterator.next();
        write(out, value, newIndent);
        newLine(out, indent);
      }
    }
    writeIndent(out, indent);
    endList(out);
  }

  public static void write(final PrintWriter out,
    final Map<String, ? extends Object> values, final String indent) {

    startObject(out, indent);
    if (values != null) {
      String newIndent = indent;
      if (newIndent != null) {
        newIndent += "  ";
      }
      final Set<String> fields = values.keySet();
      int i = 0;
      final int size = fields.size();
      final Iterator<String> iterator = fields.iterator();
      while (i < size - 1) {
        final String key = iterator.next();
        final Object value = values.get(key);
        label(out, key, newIndent);
        write(out, value, newIndent);
        endAttribute(out, indent);
        i++;
      }
      if (iterator.hasNext()) {
        final String key = iterator.next();
        final Object value = values.get(key);
        label(out, key, newIndent);
        write(out, value, newIndent);
        newLine(out, indent);
      }
    }
    writeIndent(out, indent);
    endObject(out);
  }

  @SuppressWarnings("unchecked")
  public static void write(final PrintWriter out, final Object value,
    final String indent) {
    if (value == null) {
      out.print("null");
    } else if (value instanceof StringPrinter) {
      final StringPrinter printer = (StringPrinter)value;
      printer.write(out);
    } else if (value instanceof Boolean) {
      out.print(value);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final double doubleValue = number.doubleValue();
      if (Double.isInfinite(doubleValue)) {
        out.print(-Double.MAX_VALUE);
      } else if (Double.isInfinite(doubleValue)) {
        out.print("null");
      } else {
        out.print(value);
      }
    } else if (value instanceof Collection) {
      final Collection<? extends Object> list = (Collection<? extends Object>)value;
      write(out, list, indent);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(out, map, indent);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      out.write('"');
      charSequence(out, string);
      out.write('"');
    } else if (value.getClass().isArray()) {
      final List<? extends Object> list = CollectionUtil.arrayToList(value);
      write(out, list, indent);
    } else {
      write(out, StringConverterRegistry.toString(value), indent);
    }

  }

  protected static void writeIndent(final PrintWriter out, final String indent) {
    if (indent != null) {
      out.print(indent);
    }
  }

  private JsonWriterUtil() {
  }

}
