package com.revolsys.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.util.UrlUtil;

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
  public Resource createRelative(String relativePath)
      throws MalformedURLException {
    try {
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substring(1);
      }
      final URL url = getURL();
      final URL relativeUrl = UrlUtil.getUrl(url, relativePath);
      return new UrlResource(relativeUrl);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to create relative URL "
          + this + " " + relativePath, e);
    }
  }

  @Override
  public boolean exists() {
    if (isFolderConnection()) {
      try {
        final File file = getFile();
        if (file == null) {
          return false;
        } else {
          return file.exists();
        }
      } catch (final IOException e) {
        return false;
      }
    } else {
      return super.exists();
    }
  }

  @Override
  public File getFile() throws IOException {
    final URL url = getURL();
    final File file = FileUtil.getFile(url);
    if (isFolderConnection()) {
      return file;
    } else {
      return file;
    }
  }

  @Override
  protected File getFile(final URI uri) throws IOException {
    return FileUtil.getFile(uri);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (isFolderConnection()) {
      final File file = getFile();
      return new FileInputStream(file);
    } else {
      return super.getInputStream();
    }
  }

  public boolean isFolderConnection() {
    try {
      final URL url = getURL();
      final String protocol = url.getProtocol();
      return "folderConnection".equalsIgnoreCase(protocol);
    } catch (final IOException e) {
      return false;
    }
  }
}
