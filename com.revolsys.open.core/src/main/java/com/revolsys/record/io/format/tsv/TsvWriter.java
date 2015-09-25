package com.revolsys.record.io.format.tsv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.revolsys.io.FileUtil;
import com.revolsys.util.WrappedException;

public class TsvWriter implements Closeable {

  /** The writer */
  private final Writer out;

  public TsvWriter(final Writer out) {
    this.out = out;
  }

  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  @Override
  public void close() {
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
          this.out.write('\t');
        }
      }
      this.out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
