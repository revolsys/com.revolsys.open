package com.revolsys.io.csv;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class CsvMapWriter extends AbstractMapWriter {
  private List<String> fieldNames;

  /** The writer */
  private final PrintWriter out;

  private final boolean useQuotes;

  private final char fieldSeparator;

  public CsvMapWriter(final Writer out) {
    this(out, CsvConstants.FIELD_SEPARATOR, true);
  }

  public CsvMapWriter(final Writer out, final char fieldSeparator,
    final boolean useQuotes) {
    this.out = new PrintWriter(out);
    this.fieldSeparator = fieldSeparator;
    this.useQuotes = useQuotes;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public void setFieldNames(final Collection<String> fieldNames) {
    assert this.fieldNames == null;
    this.fieldNames = new ArrayList<String>(fieldNames);
    write(fieldNames);
  }

  public void write(final Collection<? extends Object> values) {
    write(values.toArray());
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    final List<Object> fieldValues = new ArrayList<Object>();
    if (this.fieldNames == null) {
      setFieldNames(values.keySet());
    }
    for (final String fieldName : this.fieldNames) {
      final Object value = values.get(fieldName);
      fieldValues.add(value);
    }
    write(fieldValues);
  }

  public void write(final Object... values) {
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        this.out.write(this.fieldSeparator);
      }
      final Object value = values[i];
      if (value != null) {
        String string = StringConverterRegistry.toString(value);
        if (this.useQuotes) {
          string = string.replaceAll("\"", "\"\"");
          this.out.write('"');
          this.out.write(string);
          this.out.write('"');
        } else {
          this.out.write(string);
        }
      }
    }
    this.out.println();
  }
}
