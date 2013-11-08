package com.revolsys.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.revolsys.io.FileUtil;

public class UrlResource extends org.springframework.core.io.UrlResource {

  public UrlResource(final String path) throws MalformedURLException {
    super(path);
  }

  public UrlResource(final URI uri) throws MalformedURLException {
    super(uri);
  }

  public UrlResource(final URL url) {
    super(url);
  }

  @Override
  public File getFile() throws IOException {
    return super.getFile();
  }

  @Override
  protected File getFile(final URI uri) throws IOException {
    return FileUtil.getFile(uri);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if ("folderConnection".equalsIgnoreCase(getURI().getScheme())) {
      final File file = getFile();
      return new FileInputStream(file);
    } else {
      return super.getInputStream();
    }
  }
}
