package com.revolsys.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class JsonMapWriter extends AbstractMapWriter {

  /** The writer */
  private PrintWriter out;

  boolean written = false;

  public JsonMapWriter(
    final Writer out) {
    this.out = new PrintWriter(out);
  }

  private void writeHeader() {
    final String callback = getProperty("jsonp");
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    this.out.print("{\"items\": [\n");
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    if (out != null) {
      try {
        out.print("\n]}\n");
        final String callback = getProperty("jsonp");
        if (callback != null) {
          out.print(");\n");
        }
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
      writeHeader();
    }
    JsonWriterUtil.write(out, values);
  }
}
