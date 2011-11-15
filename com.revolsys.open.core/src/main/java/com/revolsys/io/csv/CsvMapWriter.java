package com.revolsys.io.csv;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class CsvMapWriter extends AbstractMapWriter {
  private List<String> fieldNames;

  /** The writer */
  private final PrintWriter out;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader The reader to the CSV file.
   */
  public CsvMapWriter(
    final Writer out) {
    this.out = new PrintWriter(out);
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    FileUtil.closeSilent(out);
  }

  public void flush() {
    out.flush();
  }

  public List<String> getFieldNames() {
    return fieldNames;
  }

  public void setFieldNames(
    final Collection<String> fieldNames) {
    assert this.fieldNames == null;
    this.fieldNames = new ArrayList<String>(fieldNames);
    write(fieldNames);
  }

  public void write(
    final Collection<? extends Object> values) {
    write(values.toArray());
  }

  public void write(
    final Map<String, ? extends Object> values) {
    final List<Object> fieldValues = new ArrayList<Object>();
    if (fieldNames == null) {
      setFieldNames(values.keySet());
    }
    for (final String fieldName : fieldNames) {
      final Object value = values.get(fieldName);
      fieldValues.add(value);
    }
    write(fieldValues);
  }

  public void write(
    final Object... values) {
    for (int i = 0; i < values.length; i++) {
      final Object value = values[i];
      if (value != null) {
        final String string = value.toString().replaceAll("\"", "\"\"");
        out.write('"');
        out.write(string);
        out.write('"');
      }
      if (i < values.length - 1) {
        out.write(',');
      }
    }
    out.println();
  }
}
