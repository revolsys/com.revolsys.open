package com.revolsys.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;

public class SpringUtil {

  public static BufferedReader getBufferedReader(final Resource resource) {
    final Reader in = getReader(resource);
    return new BufferedReader(in);
  }

  public static File getFileOrCreateTempFile(final Resource resource)
    throws IOException {
    if (resource instanceof FileSystemResource) {
      return resource.getFile();
    } else {
      final String filename = resource.getFilename();
      final String baseName = FileUtil.getBaseName(filename);
      final String fileExtension = FileUtil.getFileNameExtension(filename);
      return File.createTempFile(baseName, fileExtension);
    }
  }

  private static OutputStream getFileOutputStream(final Resource resource)
    throws IOException, FileNotFoundException {
    final File file = resource.getFile();
    return new FileOutputStream(file);
  }

  private static InputStream getInputStream(final Resource resource) {
    try {
      return resource.getInputStream();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open stream to resource "
        + resource, e);
    }
  }

  public static OutputStream getOutputStream(final Resource resource) {
    try {
      if (resource instanceof OutputStreamResource) {
        final OutputStreamResource outputStreamResource = (OutputStreamResource)resource;
        return outputStreamResource.getOutputStream();
      } else if (resource instanceof FileSystemResource) {
        return getFileOutputStream(resource);
      } else {
        final URL url = resource.getURL();
        final String protocol = url.getProtocol();
        if (protocol.equals("file")) {
          return getFileOutputStream(resource);
        } else {
          final URLConnection connection = url.openConnection();
          connection.setDoOutput(true);
          return connection.getOutputStream();
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open stream for " + resource, e);
    }
  }

  public static PrintWriter getPrintWriter(final Resource resource) {
    final Writer writer = getWriter(resource);
    return new PrintWriter(writer);
  }

  private static Reader getReader(final Resource resource) {
    final InputStream in = getInputStream(resource);
    return new InputStreamReader(in);
  }

  public static Resource getResourceWithExtension(final Resource resource,
    final String extension) throws IOException {
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    final String newFileName = baseName + "." + extension;
    return resource.createRelative(newFileName);
  }

  public static Writer getWriter(final Resource resource) {
    final OutputStream stream = getOutputStream(resource);
    return new OutputStreamWriter(stream);
  }
}
