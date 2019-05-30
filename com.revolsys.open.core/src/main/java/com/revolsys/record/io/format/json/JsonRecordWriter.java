package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.number.Numbers;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class JsonRecordWriter extends AbstractRecordWriter {

  private int depth = 0;

  private Writer out;

  private RecordDefinition recordDefinition;

  private boolean singleObject;

  private boolean startAttribute;

  private boolean written;

  public JsonRecordWriter(final RecordDefinition recordDefinition, final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = out;
  }

  private void charSequence(final CharSequence string) throws IOException {
    final Writer out = this.out;
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

  @Override
  public void close() {
    try {
      final Writer out = this.out;
      if (out != null) {
        if (!this.singleObject) {
          out.write("\n]}\n");
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          out.write(");\n");
        }
      }
    } catch (final IOException e) {
    } finally {
      FileUtil.closeSilent(this.out);
      this.out = null;
      this.recordDefinition = null;
    }
  }

  private void endAttribute() throws IOException {
    this.out.write(",\n");
    this.startAttribute = false;
  }

  private void endList() throws IOException {
    final Writer out = this.out;
    this.depth--;
    out.write('\n');
    indent();
    out.write(']');
  }

  private void endObject() throws IOException {
    final Writer out = this.out;
    this.depth--;
    out.write('\n');
    indent();
    out.write('}');
  }

  @Override
  public void flush() {
    try {
      final Writer out = this.out;
      if (out != null) {
        out.flush();
      }
    } catch (final IOException e) {
    }
  }

  private void indent() throws IOException {
    final Writer out = this.out;
    if (isIndent()) {
      for (int i = 0; i < this.depth; i++) {
        out.write("  ");
      }
    }
  }

  private void label(final String key) throws IOException {
    final Writer out = this.out;
    indent();
    string(key);
    out.write(":");
    if (isIndent()) {
      out.write(" ");
    }
    this.startAttribute = true;
  }

  private void list(final List<? extends Object> values) throws IOException {
    startList();
    int i = 0;
    final int size = values.size();
    final Iterator<? extends Object> iterator = values.iterator();
    while (i < size - 1) {
      final Object value = iterator.next();
      value(null, value);
      endAttribute();
      i++;
    }
    if (iterator.hasNext()) {
      final Object value = iterator.next();
      value(null, value);
    }
    endList();
  }

  private void startList() throws IOException {
    final Writer out = this.out;
    if (!this.startAttribute) {
      indent();
    }
    out.write("[\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void startObject() throws IOException {
    final Writer out = this.out;
    if (!this.startAttribute) {
      indent();
    }
    out.write("{\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void string(final CharSequence string) throws IOException {
    final Writer out = this.out;
    out.write('"');
    charSequence(string);
    out.write('"');
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath().toString();
  }

  @SuppressWarnings("unchecked")
  private void value(final DataType dataType, final Object value) throws IOException {
    final Writer out = this.out;
    if (value == null) {
      out.write("null");
    } else if (value instanceof Boolean) {
      if ((Boolean)value) {
        out.write("true");
      } else {
        out.write("false");
      }
    } else if (value instanceof Number) {
      out.write(Numbers.toString((Number)value));
    } else if (value instanceof List) {
      final List<? extends Object> list = (List<? extends Object>)value;
      list(list);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(map);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      string(string);
    } else if (dataType == null) {
      string(value.toString());
    } else {
      final String string = dataType.toString(value);
      string(string);
    }
  }

  private void write(final Map<String, ? extends Object> values) throws IOException {
    startObject();
    boolean first = true;
    for (final Entry<String, ? extends Object> entry : values.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      if (value != null) {
        if (!first) {
          endAttribute();
        }
        label(key);
        value(null, value);
        first = false;
      }
    }
    endObject();
  }

  @Override
  public void write(final Record record) {
    try {
      final Writer out = this.out;
      if (this.written) {
        out.write(",\n");
      } else {
        writeHeader();
      }
      startObject();
      boolean hasValue = false;
      final int attributeCount = this.recordDefinition.getFieldCount();
      for (int i = 0; i < attributeCount; i++) {
        final Object value;
        if (isWriteCodeValues()) {
          value = record.getValue(i);
        } else {
          value = record.getCodeValue(i);
        }
        if (isValueWritable(value)) {
          if (hasValue) {
            endAttribute();
          } else {
            hasValue = true;
          }
          final String name = this.recordDefinition.getFieldName(i);
          final DataType dataType = this.recordDefinition.getFieldType(i);
          label(name);
          value(dataType, value);
        }
      }
      endObject();
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  private void writeHeader() throws IOException {
    final Writer out = this.out;
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.write(callback);
      out.write('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      out.write("{\"items\": [\n");
    }
    this.written = true;
  }
}
