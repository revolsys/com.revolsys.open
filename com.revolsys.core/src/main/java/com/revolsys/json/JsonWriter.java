package com.revolsys.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JsonWriter {
  private PrintWriter out;

  private boolean indent;

  private int depth = 0;

  boolean startAttribute;

  public JsonWriter(
    Writer out) {
    if (out instanceof PrintWriter) {
      this.out = (PrintWriter)out;
    } else {
      this.out = new PrintWriter(out);
    }
  }

  public void charSequence(
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

  public void endAttribute() {
    out.print(",\n");
    startAttribute = false;
  }

  public void endList() {
    depth--;
    out.print('\n');
    indent();
    out.print("]");
  }

  public void endObject() {
    depth--;
    out.print('\n');
    indent();
    out.print("}");
  }

  public void indent() {
    if (indent) {
      for (int i = 0; i < depth; i++) {
        out.write("  ");
      }
    }
  }

  public void label(
    final String key) {
    indent();
    value(key);
    out.print(": ");
    startAttribute = true;
  }

  public void startList() {
    if (!startAttribute) {
      indent();
    }
    out.print("[\n");
    depth++;
    startAttribute = false;
  }

  public void startObject() {
    if (!startAttribute) {
      indent();
    }
    out.print("{\n");
    depth++;
    startAttribute = false;
  }

  public void list(
    final List<? extends Object> values) {
    startList();
    int i = 0;
    final int size = values.size();
    final Iterator<? extends Object> iterator = values.iterator();
    while (i < size - 1) {
      final Object value = iterator.next();
      value(value);
      endAttribute();
      i++;
    }
    if (iterator.hasNext()) {
      final Object value = iterator.next();
      value(value);
    }
    endList();
  }

  public void write(
    final Map<String, ? extends Object> values) {
    startObject();

    final Set<String> fields = values.keySet();
    int i = 0;
    final int size = fields.size();
    final Iterator<String> iterator = fields.iterator();
    while (i < size - 1) {
      final String key = iterator.next();
      final Object value = values.get(key);
      label(key);
      value(value);
      endAttribute();
      i++;
    }
    if (iterator.hasNext()) {
      final String key = iterator.next();
      final Object value = values.get(key);
      label(key);
      value(value);
    }
    endObject();
  }

  @SuppressWarnings("unchecked")
  public void value(
    final Object value) {
    if (value == null) {
      out.print("null");
    } else if (value instanceof Boolean) {
      out.print(value);
    } else if (value instanceof Number) {
      out.print(value);
    } else if (value instanceof List) {
      final List<? extends Object> list = (List<? extends Object>)value;
      list(list);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(map);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      out.print('"');
      charSequence(string);
      out.print('"');
    } else {
      value(value.toString());
    }
  }

  public void print(
    final Object value) {
    out.print(value);
  }

  public void print(
    final char value) {
    out.print(value);
  }

  public void close() {
    out.close();
  }

  public void flush() {
    out.flush();
  }

  public void setIndent(
    boolean indent) {
    this.indent = indent;
  }
}
