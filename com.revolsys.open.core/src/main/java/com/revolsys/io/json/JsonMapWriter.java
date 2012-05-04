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
    final JsonWriter jsonWriter = new JsonWriter(writer, false);
    jsonWriter.write(values);
    jsonWriter.close();
    return writer.toString();
  }

  /** The writer */
  private PrintWriter out;

  boolean written = false;

  private boolean singleObject;

  private boolean listRoot;

  private final boolean indent;

  public JsonMapWriter(final Writer out) {
    this(out, true);
  }

  public JsonMapWriter(final Writer out, final boolean indent) {
    this.out = new PrintWriter(out);
    this.indent = indent;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (out != null) {
      if (!written) {
        writeHeader();
      }
      try {
        if (!singleObject) {
          newLine();
          if (listRoot) {
            out.print("]");
          } else {
            out.print("]}");
          }
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          out.print(");");
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

  public boolean isListRoot() {
    return listRoot;
  }

  private void newLine() {
    if (indent) {
      out.print('\n');
    }
  }

  public void setListRoot(final boolean listRoot) {
    this.listRoot = listRoot;
  }

  public void write(final Map<String, ? extends Object> values) {
    if (written) {
      out.print(",");
      newLine();
    } else {
      writeHeader();
    }
    JsonWriterUtil.write(out, values);
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.print(callback);
      out.print('(');
    }
    listRoot = Boolean.TRUE.equals(getProperty(IoConstants.JSON_LIST_ROOT_PROPERTY));
    singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));

    if (!singleObject) {
      if (listRoot) {
        out.print("[");
        newLine();
      } else {
        out.print("{\"items\": [");
        newLine();
      }
    }
    written = true;
  }
}
