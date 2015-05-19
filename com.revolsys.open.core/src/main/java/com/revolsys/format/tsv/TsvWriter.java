package com.revolsys.format.tsv;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.revolsys.io.FileUtil;
import com.revolsys.util.WrappedException;

public class TsvWriter implements AutoCloseable {

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
    FileUtil.closeSilent(out);
  }

  public void flush() {
    try {
      out.flush();
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
          out.write('"');
          for (int j = 0; j < string.length(); j++) {
            final char c = string.charAt(j);
            if (c == '"') {
              out.write('"');
            }
            out.write(c);
          }
          out.write('"');
        }
        if (i < values.length - 1) {
          out.write('\t');
        }
      }
      out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
