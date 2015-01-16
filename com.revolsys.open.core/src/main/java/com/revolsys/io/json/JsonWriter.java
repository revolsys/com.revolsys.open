package com.revolsys.io.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;

public final class JsonWriter implements AutoCloseable {

  private int depth = 0;

  private boolean indent;

  private PrintWriter out;

  private boolean startAttribute;

  public JsonWriter(final Writer out) {
    this(out, true);
  }

  public JsonWriter(final Writer out, final boolean indent) {
    if (out instanceof PrintWriter) {
      this.out = (PrintWriter)out;
    } else {
      this.out = new PrintWriter(out);
    }
    this.indent = indent;
  }

  public void charSequence(final CharSequence string) {
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      switch (c) {
        case '"':
          this.out.print("\\\"");
          break;
        case '\\':
          this.out.print("\\\\");
          break;
        case '\b':
          this.out.print("\\b");
          break;
        case '\f':
          this.out.print("\\f");
          break;
        case '\n':
          this.out.print("\\n");
          break;
        case '\r':
          this.out.print("\\r");
          break;
        case '\t':
          this.out.print("\\t");
          break;
        default:
          this.out.print(c);
          break;
      }
    }
  }

  @Override
  public void close() {
    this.out.close();
  }

  public void endAttribute() {
    this.out.print(",");
    newLine();
    this.startAttribute = false;
  }

  public void endList() {
    this.depth--;
    newLine();
    indent();
    this.out.print("]");
  }

  public void endObject() {
    this.depth--;
    newLine();
    indent();
    this.out.print("}");
  }

  public void flush() {
    this.out.flush();
  }

  public void indent() {
    if (this.indent) {
      for (int i = 0; i < this.depth; i++) {
        this.out.write("  ");
      }
    }
  }

  public void label(final String key) {
    indent();
    value(key);
    this.out.print(": ");
    this.startAttribute = true;
  }

  public void list(final Object... values) {
    write(Arrays.asList(values));
  }

  public void newLine() {
    if (this.indent) {
      this.out.print('\n');
    }
  }

  public void print(final char value) {
    this.out.print(value);
  }

  public void print(final Object value) {
    this.out.print(value);
  }

  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  public void startList() {
    final boolean indent = true;
    startList(indent);
  }

  public void startList(final boolean indent) {
    if (indent && !this.startAttribute) {
      indent();
    }
    this.out.print("[");
    newLine();
    this.depth++;
    this.startAttribute = false;
  }

  public void startObject() {
    if (!this.startAttribute) {
      indent();
    }
    this.out.print("{");
    newLine();
    this.depth++;
    this.startAttribute = false;
  }

  @SuppressWarnings("unchecked")
  public void value(final Object value) {
    if (value == null) {
      this.out.print("null");
    } else if (value instanceof Boolean) {
      this.out.print(value);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final double doubleValue = number.doubleValue();
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        this.out.print("null");
      } else {
        this.out.print(MathUtil.toString(doubleValue));
      }
    } else if (value instanceof Collection) {
      final Collection<? extends Object> list = (Collection<? extends Object>)value;
      write(list);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(map);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      this.out.print('"');
      charSequence(string);
      this.out.print('"');
    } else if (value.getClass().isArray()) {
      final List<? extends Object> list = CollectionUtil.arrayToList(value);
      write(list);
    } else {
      value(StringConverterRegistry.toString(value));
    }
  }

  public void write(final Collection<? extends Object> values) {
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

  public void write(final Map<String, ? extends Object> values) {
    startObject();
    if (values != null) {
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
    }
    endObject();
  }
}
