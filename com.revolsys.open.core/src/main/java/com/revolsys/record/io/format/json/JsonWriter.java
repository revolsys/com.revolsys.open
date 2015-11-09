package com.revolsys.record.io.format.json;

import java.io.Closeable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.list.Lists;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.io.FileUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.WrappedException;

public final class JsonWriter implements Closeable {

  private int depth = 0;

  private boolean indent;

  private Writer out;

  private boolean startAttribute;

  public JsonWriter(final Writer out) {
    this(out, true);
  }

  public JsonWriter(final Writer out, final boolean indent) {
    this.out = out;
    this.indent = indent;
  }

  public void charSequence(final CharSequence string) {
    try {
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        switch (c) {
          case '"':
            this.out.write("\\\"");
          break;
          case '\\':
            this.out.write("\\\\");
          break;
          case '\b':
            this.out.write("\\b");
          break;
          case '\f':
            this.out.write("\\f");
          break;
          case '\n':
            this.out.write("\\n");
          break;
          case '\r':
            this.out.write("\\r");
          break;
          case '\t':
            this.out.write("\\t");
          break;
          default:
            this.out.write(c);
          break;
        }
      }
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
    this.out = null;
  }

  public void endAttribute() {
    try {
      this.out.write(",");
      newLine();
      this.startAttribute = false;
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void endList() {
    try {
      this.depth--;
      newLine();
      indent();
      this.out.write("]");
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void endObject() {
    try {
      this.depth--;
      newLine();
      indent();
      this.out.write("}");
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void flush() {
    try {
      this.out.flush();
    } catch (final Exception e) {
    }
  }

  public void indent() {
    try {
      if (this.indent) {
        for (int i = 0; i < this.depth; i++) {
          this.out.write("  ");
        }
      }
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void label(final String key) {
    try {
      indent();
      value(key);
      this.out.write(": ");
      this.startAttribute = true;
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void list(final Object... values) {
    write(Arrays.asList(values));
  }

  public void newLine() {
    try {
      if (this.indent) {
        this.out.write('\n');
      }
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void print(final char value) {
    try {
      this.out.write(value);
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void print(final Object value) {
    if (value != null) {
      try {
        this.out.write(value.toString());
      } catch (final Exception e) {
        throw new WrappedException(e);
      }
    }
  }

  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  public void startList() {
    final boolean indent = true;
    startList(indent);
  }

  public void startList(final boolean indent) {
    try {
      if (indent && !this.startAttribute) {
        indent();
      }
      this.out.write('[');
      newLine();
      this.depth++;
      this.startAttribute = false;
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  public void startObject() {
    try {
      if (!this.startAttribute) {
        indent();
      }
      this.out.write('{');
      newLine();
      this.depth++;
      this.startAttribute = false;
    } catch (final Exception e) {
      throw new WrappedException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public void value(final Object value) {
    try {
      if (value == null) {
        this.out.write("null");
      } else if (value instanceof Boolean) {
        if ((Boolean)value) {
          this.out.write("true");
        } else {
          this.out.write("false");
        }
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        final double doubleValue = number.doubleValue();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
          this.out.write("null");
        } else {
          this.out.write(MathUtil.toString(doubleValue));
        }
      } else if (value instanceof Collection) {
        final Collection<? extends Object> list = (Collection<? extends Object>)value;
        write(list);
      } else if (value instanceof Map) {
        final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
        write(map);
      } else if (value instanceof CharSequence) {
        final CharSequence string = (CharSequence)value;
        this.out.write('"');
        charSequence(string);
        this.out.write('"');
      } else if (value.getClass().isArray()) {
        final List<? extends Object> list = Lists.arrayToList(value);
        write(list);
      } else {
        value(StringConverter.toString(value));
      }
    } catch (final Exception e) {
      throw new WrappedException(e);
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
