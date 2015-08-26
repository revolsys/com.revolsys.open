package com.revolsys.format.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.util.WrappedException;

public class CsvMapWriter extends AbstractMapWriter {
  private List<String> fieldNames;

  private final char fieldSeparator;

  /** The writer */
  private final Writer out;

  private final boolean useQuotes;

  public CsvMapWriter(final File file) throws FileNotFoundException {
    this(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
  }

  public CsvMapWriter(final Writer out) {
    this(out, Csv.FIELD_SEPARATOR, true);
  }

  public CsvMapWriter(final Writer out, final char fieldSeparator, final boolean useQuotes) {
    this.out = new BufferedWriter(out);
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
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
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
    try {
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          this.out.write(this.fieldSeparator);
        }
        final Object value = values[i];
        if (value != null) {
          final String string = StringConverterRegistry.toString(value);
          if (this.useQuotes) {
            this.out.write('"');
            for (int j = 0; j < string.length(); j++) {
              final char c = string.charAt(j);
              if (c == '"') {
                this.out.write('"');
              }
              this.out.write(c);
            }
            this.out.write('"');
          } else {
            this.out.write(string, 0, string.length());
          }
        }
      }
      this.out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
