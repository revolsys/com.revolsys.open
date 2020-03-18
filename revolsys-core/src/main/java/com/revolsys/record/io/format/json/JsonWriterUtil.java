package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.StringPrinter;

public final class JsonWriterUtil {
  public static void charSequence(final Writer out, final CharSequence string) throws IOException {
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      switch (c) {
        case '"':
          out.write("\\\"");
        break;
        case '\\':
          out.write("\\\\");
        break;
        case '\b':
          out.write("\\b");
        break;
        case '\f':
          out.write("\\f");
        break;
        case '\n':
          out.write("\\n");
        break;
        case '\r':
          out.write("\\r");
        break;
        case '\t':
          out.write("\\t");
        break;
        default:
          out.write(c);
        break;
      }
    }
  }

  public static void endAttribute(final Writer out, final String indent) throws IOException {
    out.write(',');
    newLine(out, indent);
  }

  public static void endList(final Writer out) throws IOException {
    out.write(']');
  }

  public static void endObject(final Writer out) throws IOException {
    out.write('}');
  }

  public static void label(final Writer out, final String key, final String indent)
    throws IOException {
    writeIndent(out, indent);
    out.write('"');
    charSequence(out, key);
    out.write('"');

    out.write(":");
  }

  public static void newLine(final Writer out, final String indent) throws IOException {
    if (indent != null) {
      out.write('\n');
    }
  }

  public static void startList(final Writer out, final String indent) throws IOException {
    out.write('[');
    newLine(out, indent);
  }

  public static void startObject(final Writer out, final String indent) throws IOException {
    out.write('{');
    newLine(out, indent);
  }

  public static void write(final Writer out, final Collection<? extends Object> values,
    final String indent, final boolean writeNulls) throws IOException {
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
        write(out, value, newIndent, writeNulls);
        endAttribute(out, indent);
        i++;
      }
      if (iterator.hasNext()) {
        writeIndent(out, newIndent);
        final Object value = iterator.next();
        write(out, value, newIndent, writeNulls);
        newLine(out, indent);
      }
    }
    writeIndent(out, indent);
    endList(out);
  }

  public static void write(final Writer out, final Map<String, ? extends Object> values,
    final String indent, final boolean writeNulls) throws IOException {

    startObject(out, indent);
    if (values != null) {
      String newIndent = indent;
      if (newIndent != null) {
        newIndent += "  ";
      }
      final Set<String> fields = values.keySet();
      boolean hasValue = false;
      for (final String key : fields) {
        if (hasValue) {
          endAttribute(out, indent);
        } else {
          hasValue = true;
        }
        final Object value = values.get(key);
        label(out, key, newIndent);
        write(out, value, newIndent, writeNulls);
      }
      if (hasValue) {
        newLine(out, newIndent);
      }
    }
    writeIndent(out, indent);
    endObject(out);
  }

  @SuppressWarnings("unchecked")
  public static void write(final Writer out, final Object value, final String indent,
    final boolean writeNulls) throws IOException {
    if (value == null) {
      out.write("null");
    } else if (value instanceof StringPrinter) {
      final StringPrinter printer = (StringPrinter)value;
      printer.write(out);
    } else if (value instanceof Boolean) {
      if ((Boolean)value) {
        out.write("true");
      } else {
        out.write("false");
      }
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      String string = Numbers.toString(number);
      if ("NaN".equals(string)) {
        string = "null";
      } else if ("Infinity".equals(string)) {
        string = Doubles.MAX_DOUBLE_STRING;
      } else if ("-Infinity".equals(string)) {
        string = Doubles.MIN_DOUBLE_STRING;
      }
      out.write(string);
    } else if (value instanceof Collection) {
      final Collection<? extends Object> list = (Collection<? extends Object>)value;
      write(out, list, indent, writeNulls);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(out, map, indent, false);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      out.write('"');
      charSequence(out, string);
      out.write('"');
    } else if (value.getClass().isArray()) {
      final List<? extends Object> list = Lists.arrayToList(value);
      write(out, list, indent, writeNulls);
    } else {
      write(out, DataTypes.toString(value), indent, writeNulls);
    }

  }

  protected static void writeIndent(final Writer out, final String indent) throws IOException {
    if (indent != null) {
      out.write(indent);
    }
  }

  private JsonWriterUtil() {
  }

}
