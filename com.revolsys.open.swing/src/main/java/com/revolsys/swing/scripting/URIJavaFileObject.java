package com.revolsys.swing.scripting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import com.revolsys.io.FileUtil;

public class URIJavaFileObject extends SimpleJavaFileObject {
  public URIJavaFileObject(final File file, final Kind kind) {
    super(file.toURI(), kind);
  }

  public URIJavaFileObject(final URI uri, final Kind kind) {
    super(uri, kind);
  }

  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
    final InputStream inputStream = openInputStream();
    return FileUtil.getString(inputStream);
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return this.uri.toURL().openStream();
  }

  @Override
  public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
    final InputStream inputStream = openInputStream();
    return FileUtil.newUtf8Reader(inputStream);
  }
}
