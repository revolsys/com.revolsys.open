package com.revolsys.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class SpringUtil {

  public static OutputStream getOutputStream(
    Resource resource)
    throws IOException {
    if (resource instanceof FileSystemResource) {
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
  }

  private static OutputStream getFileOutputStream(
    Resource resource)
    throws IOException,
    FileNotFoundException {
    final File file = resource.getFile();
    return new FileOutputStream(file);
  }
}
