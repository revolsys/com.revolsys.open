package com.revolsys.format.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.revolsys.io.FileUtil;
import com.revolsys.util.WrappedException;

public class CsvWriter implements Closeable {

  /** The writer */
  private final Writer out;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   *
   * @param reader The reader to the CSV file.
   * @throws IOException
   */
  public CsvWriter(final Writer out) {
    this.out = new BufferedWriter(out);
  }

  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  @Override
  public void close() {
    flush();
    FileUtil.closeSilent(this.out);
  }

  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
  }

  public void write(final Collection<? extends Object> values) {
    write(values.toArray());
  }

  public void write(final Object... values) {
    try {
      for (int i = 0; i < values.length; i++) {
        final Object value = values[i];
        if (value != null) {
          final String string = value.toString();
          this.out.write('"');
          for (int j = 0; j < string.length(); j++) {
            final char c = string.charAt(j);
            if (c == '"') {
              this.out.write('"');
            }
            this.out.write(c);
          }
          this.out.write('"');
        }
        if (i < values.length - 1) {
          this.out.write(',');
        }
      }
      this.out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
