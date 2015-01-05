package com.revolsys.io.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;

public class JsonMapWriter extends AbstractMapWriter {
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
    if (this.out != null) {
      if (!this.written) {
        writeHeader();
      }
      try {
        if (!this.singleObject) {
          newLine();
          if (this.listRoot) {
            this.out.print("]");
          } else {
            this.out.print("]}");
          }
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          this.out.print(");");
        }
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

  public boolean isListRoot() {
    return this.listRoot;
  }

  private void newLine() {
    if (this.indent) {
      this.out.print('\n');
    }
  }

  public void setListRoot(final boolean listRoot) {
    this.listRoot = listRoot;
  }

  public void setSingleObject(final boolean singleObject) {
    setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, singleObject);
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    if (this.written) {
      this.out.print(",");
      newLine();
    } else {
      writeHeader();
    }
    String indentString = null;
    if (this.indent) {
      if (this.singleObject) {
        indentString = "";
      } else {
        indentString = "  ";
        this.out.print(indentString);
      }
    }
    JsonWriterUtil.write(this.out, values, indentString, isWriteNulls());
    newLine();
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    this.listRoot = Boolean.TRUE.equals(getProperty(IoConstants.JSON_LIST_ROOT_PROPERTY));
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));

    if (!this.singleObject) {
      if (this.listRoot) {
        this.out.print("[");
        newLine();
      } else {
        this.out.print("{\"items\": [");
        newLine();
      }
    }
    this.written = true;
  }
}
