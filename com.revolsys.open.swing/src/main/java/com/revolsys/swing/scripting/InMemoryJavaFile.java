package com.revolsys.swing.scripting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import javax.tools.SimpleJavaFileObject;

public class InMemoryJavaFile extends SimpleJavaFileObject {

  private byte[] data = new byte[0];

  private long lastModified = 0L;

  protected InMemoryJavaFile(final URI uri, final Kind kind) {
    super(uri, kind);
  }

  @Override
  public boolean delete() {
    this.data = null;
    this.lastModified = 0;
    return super.delete();
  }

  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
    if (this.data.length == 0) {
      throw new FileNotFoundException();
    } else {
      return new String(this.data, Charset.defaultCharset());
    }
  }

  public byte[] getData() {
    return this.data;
  }

  @Override
  public long getLastModified() {
    return this.lastModified;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    if (this.data.length == 0) {
      throw new FileNotFoundException();
    } else {
      return new ByteArrayInputStream(this.data);
    }
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        super.close();
        InMemoryJavaFile.this.data = toByteArray();
        InMemoryJavaFile.this.lastModified = System.currentTimeMillis();
      }
    };
  }
}
