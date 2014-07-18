package com.revolsys.io.json;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.util.MathUtil;

public class JsonRecordWriter extends AbstractRecordWriter {

  private RecordDefinition recordDefinition;

  private PrintWriter out;

  private int depth = 0;

  boolean startAttribute;

  private boolean singleObject;

  private boolean written;

  public JsonRecordWriter(final RecordDefinition recordDefinition,
    final java.io.Writer out) {
    this.recordDefinition = recordDefinition;
    if (out instanceof PrintWriter) {
      this.out = (PrintWriter)out;
    } else {
      this.out = new PrintWriter(out);
    }
  }

  private void charSequence(final CharSequence string) {
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
    if (this.out != null) {
      try {
        if (!this.singleObject) {
          this.out.print("\n]}\n");
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          this.out.print(");\n");
        }
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
      }
    }
    this.recordDefinition = null;
  }

  private void endAttribute() {
    this.out.print(",\n");
    this.startAttribute = false;
  }

  private void endList() {
    this.depth--;
    this.out.print('\n');
    indent();
    this.out.print("]");
  }

  private void endObject() {
    this.depth--;
    this.out.print('\n');
    indent();
    this.out.print("}");
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  private void indent() {
    if (isIndent()) {
      for (int i = 0; i < this.depth; i++) {
        this.out.write("  ");
      }
    }
  }

  private void label(final String key) {
    indent();
    string(key);
    this.out.print(":");
    if (isIndent()) {
      this.out.print(" ");
    }
    this.startAttribute = true;
  }

  private void list(final List<? extends Object> values) {
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

  private void startList() {
    if (!this.startAttribute) {
      indent();
    }
    this.out.print("[\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void startObject() {
    if (!this.startAttribute) {
      indent();
    }
    this.out.print("{\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void string(final CharSequence string) {
    this.out.print('"');
    charSequence(string);
    this.out.print('"');
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath().toString();
  }

  @SuppressWarnings("unchecked")
  private void value(final DataType dataType, final Object value) {
    if (value == null) {
      this.out.print("null");
    } else if (value instanceof Boolean) {
      this.out.print(value);
    } else if (value instanceof Number) {
      this.out.print(MathUtil.toString((Number)value));
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
      final String string = StringConverterRegistry.toString(dataType, value);
      string(string);
    }
  }

  private void write(final Map<String, ? extends Object> values) {
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
  public void write(final Record object) {
    if (this.written) {
      this.out.print(",\n");
    } else {
      writeHeader();
    }
    startObject();
    boolean hasValue = false;
    final int attributeCount = this.recordDefinition.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      if (isWritable(value)) {
        if (hasValue) {
          endAttribute();
        } else {
          hasValue = true;
        }
        final String name = this.recordDefinition.getAttributeName(i);
        final DataType dataType = this.recordDefinition.getAttributeType(i);
        label(name);
        value(dataType, value);
      }
    }
    endObject();
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      this.out.print("{\"items\": [\n");
    }
    this.written = true;
  }
}
