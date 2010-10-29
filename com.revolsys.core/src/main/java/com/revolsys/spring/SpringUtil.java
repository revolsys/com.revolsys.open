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

import com.revolsys.io.FileUtil;

public class SpringUtil {

  public static OutputStream getOutputStream(
    Resource resource)
    throws IOException {
    if (resource instanceof OutputStreamResource) {
      OutputStreamResource outputStreamResource = (OutputStreamResource)resource;
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
  }

  public static File getFileOrCreateTempFile(
    Resource resource)
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

  public static Resource getResourceWithExtension(
    Resource resource,
    String extension)
    throws IOException {
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    final String newFileName = baseName + "." + extension;
    return resource.createRelative(newFileName);
  }

  private static OutputStream getFileOutputStream(
    Resource resource)
    throws IOException,
    FileNotFoundException {
    final File file = resource.getFile();
    return new FileOutputStream(file);
  }
}
