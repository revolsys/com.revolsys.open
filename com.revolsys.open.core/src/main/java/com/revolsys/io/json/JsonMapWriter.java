package com.revolsys.io.json;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;

public class JsonMapWriter extends AbstractMapWriter {
  public static String toString(final Map<String, ? extends Object> values) {
    final StringWriter writer = new StringWriter();
    final JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.write(values);
    jsonWriter.close();
    return writer.toString();
  }

  /** The writer */
  private PrintWriter out;

  boolean written = false;

  private boolean singleObject;

  public JsonMapWriter(final Writer out) {
    this.out = new PrintWriter(out);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (out != null) {
      try {
        if (!singleObject) {
          out.print("\n]}\n");
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          out.print(");\n");
        }
      } finally {
        FileUtil.closeSilent(out);
        out = null;
      }
    }
  }

  @Override
  public void flush() {
    out.flush();
  }

  public void write(final Map<String, ? extends Object> values) {
    if (written) {
      out.print(",\n");
    } else {
      writeHeader();
    }
    JsonWriterUtil.write(out, values);
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!singleObject) {
      this.out.print("{\"items\": [\n");
    }
    written = true;
  }
}
