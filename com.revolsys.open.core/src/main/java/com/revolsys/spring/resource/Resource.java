package com.revolsys.spring.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.revolsys.io.FileNames;
import com.revolsys.io.FileUtil;
import com.revolsys.util.WrappedException;

public interface Resource extends org.springframework.core.io.Resource {
  static boolean exists(final com.revolsys.spring.resource.Resource resource) {
    if (resource == null) {
      return false;
    } else {
      return resource.exists();
    }
  }

  static com.revolsys.spring.resource.Resource getResource(final Object source) {
    com.revolsys.spring.resource.Resource resource;
    if (source instanceof com.revolsys.spring.resource.Resource) {
      resource = (com.revolsys.spring.resource.Resource)source;
    } else if (source instanceof Path) {
      resource = new PathResource((Path)source);
    } else if (source instanceof File) {
      resource = new FileSystemResource((File)source);
    } else if (source instanceof URL) {
      resource = new UrlResource((URL)source);
    } else if (source instanceof URI) {
      resource = new UrlResource((URI)source);
    } else if (source instanceof String) {
      return SpringUtil.getResource((String)source);
    } else {
      throw new IllegalArgumentException(source.getClass() + " is not supported");
    }
    return resource;
  }

  default String contentsAsString() {
    final Reader reader = newReader();
    return FileUtil.getString(reader);
  }

  @Override
  Resource createRelative(String relativePath);

  default boolean delete() {
    return false;
  }

  default String getBaseName() {
    final String filename = getFilename();
    return FileNames.getBaseName(filename);
  }

  @Override
  File getFile();

  default String getFileNameExtension(final Resource resource) {
    final String filename = resource.getFilename();
    return FileNames.getFileNameExtension(filename);
  }

  @Override
  InputStream getInputStream();

  default Resource getParent() {
    return null;
  }

  default Resource getResourceWithExtension(final String extension) {
    final String baseName = getBaseName();
    final String newFileName = baseName + "." + extension;
    final Resource parent = getParent();
    if (parent == null) {
      return null;
    } else {
      return parent.createRelative(newFileName);
    }
  }

  @Override
  URL getURL();

  default OutputStream newBufferedOutputStream() {
    final OutputStream out = newOutputStream();
    return new BufferedOutputStream(out);
  }

  default InputStream newInputStream() {
    return getInputStream();
  }

  default OutputStream newOutputStream() {
    try {
      final URL url = getURL();
      final String protocol = url.getProtocol();
      if (protocol.equals("file") || protocol.equals("folderconnection")) {
        final File file = getFile();
        return new FileOutputStream(file);
      } else {
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        return connection.getOutputStream();
      }
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  default Reader newReader() {
    final InputStream in = getInputStream();
    return FileUtil.createUtf8Reader(in);
  }

  default Writer newWriter() {
    final OutputStream stream = newOutputStream();
    return FileUtil.createUtf8Writer(stream);
  }

  default Writer newWriter(final Charset charset) {
    final OutputStream stream = newOutputStream();
    return new OutputStreamWriter(stream, charset);
  }
}
