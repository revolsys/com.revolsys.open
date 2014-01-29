package com.revolsys.io.json;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.util.MathUtil;

public class JsonDataObjectWriter extends AbstractWriter<DataObject> {

  private DataObjectMetaData metaData;

  private PrintWriter out;

  private int depth = 0;

  private boolean indent;

  boolean startAttribute;

  private boolean singleObject;

  private boolean written;

  public JsonDataObjectWriter(final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
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

  @Override
  public void close() {
    if (out != null) {
      try {
        if (!singleObject) {
          out.print("\n]}\n");
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          out.print(");\n");
        }
      } finally {
        FileUtil.closeSilent(out);
        out = null;
      }
    }
    metaData = null;
  }

  private void endAttribute() {
    out.print(",\n");
    startAttribute = false;
  }

  private void endList() {
    depth--;
    out.print('\n');
    indent();
    out.print("]");
  }

  private void endObject() {
    depth--;
    out.print('\n');
    indent();
    out.print("}");
  }

  @Override
  public void flush() {
    out.flush();
  }

  private void indent() {
    if (indent) {
      for (int i = 0; i < depth; i++) {
        out.write("  ");
      }
    }
  }

  private void label(final String key) {
    indent();
    string(key);
    out.print(":");
    if (indent) {
      out.print(" ");
    }
    startAttribute = true;
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

  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  private void startList() {
    if (!startAttribute) {
      indent();
    }
    out.print("[\n");
    depth++;
    startAttribute = false;
  }

  private void startObject() {
    if (!startAttribute) {
      indent();
    }
    out.print("{\n");
    depth++;
    startAttribute = false;
  }

  private void string(final CharSequence string) {
    out.print('"');
    charSequence(string);
    out.print('"');
  }

  @Override
  public String toString() {
    return metaData.getPath().toString();
  }

  @SuppressWarnings("unchecked")
  private void value(final DataType dataType, final Object value) {
    if (value == null) {
      out.print("null");
    } else if (value instanceof Boolean) {
      out.print(value);
    } else if (value instanceof Number) {
      out.print(MathUtil.toString((Number)value));
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

  @Override
  public void write(final DataObject object) {
    if (written) {
      out.print(",\n");
    } else {
      writeHeader();
    }
    startObject();
    boolean first = true;
    final int attributeCount = metaData.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      if (value != null) {
        if (!first) {
          endAttribute();
        }
        final String name = metaData.getAttributeName(i);
        final DataType dataType = metaData.getAttributeType(i);
        label(name);
        value(dataType, value);
        first = false;
      }
    }
    endObject();
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

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!singleObject) {
      this.out.print("{\"items\": [\n");
    }
    written = true;
  }
}
