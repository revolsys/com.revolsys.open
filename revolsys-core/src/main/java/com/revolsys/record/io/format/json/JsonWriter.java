package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

enum JsonState {
  START_DOCUMENT, START_OBJECT('{', '}'), END_OBJECT('}'), START_LIST('[',
    ']'), END_LIST(']'), VALUE, LABEL, END_ATTRIBUTE;

  private char c;

  private char endChar;

  private JsonState() {
  }

  private JsonState(final char c) {
    this.c = c;
  }

  private JsonState(final char c, final char endChar) {
    this.c = c;
    this.endChar = endChar;
  }

  public char getChar() {
    return this.c;
  }

  public char getEndChar() {
    return this.endChar;
  }

}

public class JsonWriter implements BaseCloseable {

  public static JsonWriter nullWriter() {
    final Writer nullWriter = Writer.nullWriter();
    return new JsonWriter(nullWriter);
  }

  private int depth = 0;

  private final List<JsonState> depthStack = new ArrayList<>();

  private boolean indent;

  private Writer out;

  private JsonState state = JsonState.START_DOCUMENT;

  private final JsonStringEncodingWriter encodingOut;

  private boolean closeTargetWriter = true;

  private boolean indented = false;

  private final boolean newlined = false;

  public JsonWriter(final OutputStream out, final boolean indent) {
    this(new OutputStreamWriter(out), indent);
  }

  public JsonWriter(final Writer out) {
    this(out, true);
  }

  public JsonWriter(final Writer out, final boolean indent) {
    this.out = out;
    this.indent = indent;
    this.encodingOut = new JsonStringEncodingWriter(out);

  }

  private void blockEnd(final JsonState startState, final JsonState endState) {
    if (this.depth > 0) {
      this.depth--;
    }

    if (this.depth < this.depthStack.size()) {
      this.depthStack.remove(this.depth);
    }
    if (this.state != startState) {
      indent();
    }
    writeState(endState);
  }

  private void blockStart(final JsonState state) {
    writeState(state);
    this.depth++;
    this.depthStack.add(state);
  }

  public void charSequence(final CharSequence string) {
    try {
      this.encodingOut.append(string);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void close() {
    final Writer out = this.out;
    if (out != null) {
      this.out = null;
      try {
        try {
          for (int i = this.depth; i > 0; i--) {
            final JsonState state = this.depthStack.remove(i - 1);
            final char endChar = state.getEndChar();
            out.write(endChar);
          }
          out.flush();
        } catch (final Exception e) {
          throw Exceptions.wrap(e);
        }
        this.depthStack.clear();
        this.depth = 0;
      } finally {
        if (this.closeTargetWriter) {
          FileUtil.closeSilent(out);
        }
      }
    }
  }

  public void endAttribute() {
    if (this.state != JsonState.END_ATTRIBUTE && this.state != JsonState.START_DOCUMENT) {
      try {
        this.out.write(',');
        setState(JsonState.END_ATTRIBUTE);
      } catch (final Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public void endList() {
    blockEnd(JsonState.START_LIST, JsonState.END_LIST);
  }

  public void endObject() {
    blockEnd(JsonState.START_OBJECT, JsonState.END_OBJECT);
  }

  public void flush() {
    try {
      this.out.flush();
    } catch (final Exception e) {
    }
  }

  public void indent() {
    if (this.indent && !this.indented) {
      this.indented = true;
      try {
        final Writer out = this.out;
        out.write('\n');
        final int depth = this.depth;
        for (int i = 0; i < depth; i++) {
          out.write("  ");
        }
      } catch (final Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public boolean isCloseTargetWriter() {
    return this.closeTargetWriter;
  }

  public void label(final String key) {
    if (this.state != JsonState.START_OBJECT && this.state != JsonState.END_ATTRIBUTE) {
      endAttribute();
    }
    try {
      indent();
      string(key);
      this.out.write(": ");
      setState(JsonState.LABEL);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void labelValue(final String key, final Object value) {
    label(key);
    value(value);
  }

  public void list(final Iterable<?> values) throws IOException {
    startList();
    if (this.indent) {
      for (final Object value : values) {
        indent();
        value(value);
      }
    } else {
      for (final Object value : values) {
        value(value);
      }
    }
    endList();
  }

  public void list(final Object... values) throws IOException {
    startList();
    if (this.indent) {
      for (final Object value : values) {
        indent();
        value(value);
      }
    } else {
      for (final Object value : values) {
        value(value);
      }
    }
    endList();
  }

  public void newLineForce() {
    try {
      this.out.write('\n');
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void print(final char value) {
    try {
      this.out.write(value);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void print(final Object value) {
    if (value != null) {
      try {
        this.out.write(value.toString());
      } catch (final Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public JsonWriter setCloseTargetWriter(final boolean closeTargetWriter) {
    this.closeTargetWriter = closeTargetWriter;
    return this;
  }

  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  private void setState(final JsonState state) {
    this.state = state;
    this.indented = false;
  }

  public void startList() {
    final boolean indent = true;
    startList(indent);
  }

  public void startList(final boolean indent) {
    final JsonState state = this.state;
    if (state == JsonState.START_LIST) {
      if (this.indent) {
        indent();
      }
    } else if (state == JsonState.START_DOCUMENT || state == JsonState.LABEL) {
    } else {
      endAttribute();
      if (indent) {
        indent();
      }
    }
    blockStart(JsonState.START_LIST);
  }

  public void startObject() {
    if (this.state == JsonState.START_LIST) {
      if (this.indent) {
        indent();
      }
    } else if (this.state == JsonState.START_DOCUMENT || this.state == JsonState.LABEL) {
    } else {
      endAttribute();
      if (this.indent) {
        indent();
      }
    }
    blockStart(JsonState.START_OBJECT);
  }

  public void string(final String string) {
    try {
      final Writer out = this.out;
      if (string == null) {
        out.write("null");
      } else {
        out.write('"');
        this.encodingOut.write(string);
        out.write('"');
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public void value(final DataType dataType, final Object value) throws IOException {
    valuePre();
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
      string(string.toString());
    } else if (dataType == null) {
      string(value.toString());
    } else {
      final String string = dataType.toString(value);
      string(string);
    }
    setState(JsonState.VALUE);

  }

  @SuppressWarnings("unchecked")
  public void value(final Object value) {
    valuePre();
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
          this.out.write(Doubles.toString(doubleValue));
        }
      } else if (value instanceof MapSerializer) {
        final JsonObject map = ((MapSerializer)value).toMap();
        write(map);
      } else if (value instanceof Collection) {
        final Collection<? extends Object> list = (Collection<? extends Object>)value;
        write(list);
      } else if (value instanceof Jsonable) {
        final JsonType json = ((Jsonable)value).toJson();
        if (value instanceof JsonObject) {
          final Map<String, ? extends Object> map = (JsonObject)value;
          write(map);
        } else if (value instanceof JsonList) {
          final List<? extends Object> list = (JsonList)value;
          write(list);
        } else {
          value(json);
        }
      } else if (value instanceof Map) {
        final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
        write(map);
      } else if (value instanceof String) {
        final String string = (String)value;
        this.out.write('"');
        this.encodingOut.write(string);
        this.out.write('"');
      } else if (value instanceof CharSequence) {
        final CharSequence string = (CharSequence)value;
        this.out.write('"');
        this.encodingOut.append(string);
        this.out.write('"');
      } else if (value.getClass().isArray()) {
        final List<? extends Object> list = Lists.arrayToList(value);
        write(list);
      } else {
        value(DataTypes.toString(value));
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
    setState(JsonState.VALUE);
  }

  private void valuePre() {
    final JsonState state = this.state;
    if (state == JsonState.LABEL) {
    } else if (state == JsonState.START_LIST) {
      if (this.indent) {
        indent();
      }
    } else {
      if (state != JsonState.END_ATTRIBUTE) {
        endAttribute();
      }
      if (this.indent) {
        indent();
      }
    }
  }

  public void write(final Collection<? extends Object> values) throws IOException {
    startList(false);
    for (final Object value : values) {
      value(value);
    }
    endList();
  }

  public <K, V> void write(final Map<K, V> values) {
    startObject();
    if (values != null) {
      for (final Entry<K, V> entry : values.entrySet()) {
        final K key = entry.getKey();
        final Object value = entry.getValue();
        label(key.toString());
        value(value);
      }
    }
    endObject();
  }

  public <K, V> void writeMap(final Map<K, V> values) {
    startObject();
    if (values != null) {
      for (final Entry<K, V> entry : values.entrySet()) {
        final K key = entry.getKey();
        final Object value = entry.getValue();
        label(key.toString());
        value(value);
      }
    }
    endObject();
  }

  public void writeNull() throws IOException {
    valuePre();
    this.out.write("null");
  }

  public void writeRecord(final Record record) {
    try {
      startObject();
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final List<FieldDefinition> fieldDefinitions = recordDefinition.getFieldDefinitions();

      for (final FieldDefinition field : fieldDefinitions) {
        final int fieldIndex = field.getIndex();
        final Object value = record.getValue(fieldIndex);
        if (Property.hasValue(value)) {
          final String name = field.getName();
          label(name);

          final DataType dataType = field.getDataType();
          value(dataType, value);
        }
      }
      endObject();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void writeState(final JsonState state) {
    try {
      setState(state);
      final char c = state.getChar();
      this.out.write(c);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }
}
