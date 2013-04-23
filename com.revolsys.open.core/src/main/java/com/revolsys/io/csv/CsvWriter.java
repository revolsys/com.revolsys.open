package com.revolsys.io.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import com.revolsys.io.FileUtil;

public class CsvWriter {

  /** The writer */
  private final PrintWriter out;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader The reader to the CSV file.
   * @throws IOException
   */
  public CsvWriter(final Writer out) {
    this.out = new PrintWriter(out);
  }

  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  public void close() {
    FileUtil.closeSilent(out);
  }

  public void flush() {
    out.flush();
  }

  public void write(final Collection<? extends Object> values) {
    write(values.toArray());
  }

  public void write(final Object... values) {
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
