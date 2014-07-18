package com.revolsys.io.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class JsonListMapWriter extends AbstractMapWriter {

  /** The writer */
  private PrintWriter out;

  boolean written = false;

  public JsonListMapWriter(final Writer out) {
    this.out = new PrintWriter(out);
    this.out.print("[");
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        this.out.print("\n]\n");
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    if (this.written) {
      this.out.print(",\n");
    } else {
      this.out.print("\n");
      this.written = true;
    }
    JsonWriterUtil.write(this.out, values, null, isWriteNulls());
  }
}
