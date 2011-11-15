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

  public JsonListMapWriter(
    final Writer out) {
    this.out = new PrintWriter(out);
    this.out.print("[");
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    if (out != null) {
      try {
        out.print("\n]\n");
      } finally {
        FileUtil.closeSilent(out);
        out = null;
      }
    }
  }

  public void flush() {
    out.flush();
  }

  public void write(
    final Map<String, ? extends Object> values) {
    if (written) {
      out.print(",\n");
    } else {
      out.print("\n");
      written = true;
    }
    JsonWriterUtil.write(out, values);
  }
}
