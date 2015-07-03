package com.revolsys.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.List;

import com.revolsys.util.WrappedException;

public class Paths {
  public static Path get(final String first, final String... more) {
    return java.nio.file.Paths.get(first, more);
  }

  public static Path get(final URI uri) {
    return java.nio.file.Paths.get(uri);
  }

  public static String getBaseName(final java.nio.file.Path path) {
    final String fileName = getFileName(path);
    return FileUtil.getBaseName(fileName);
  }

  public static String getFileName(final Path path) {
    if (path.getNameCount() == 0) {
      final String fileName = path.toString();
      if (fileName.endsWith("\\") || fileName.endsWith("\\")) {
        return fileName.substring(0, fileName.length() - 1);
      } else {
        return fileName;
      }
    } else {
      final Path fileNamePath = path.getFileName();
      final String fileName = fileNamePath.toString();
      if (fileName.endsWith("\\") || fileName.endsWith("/")) {
        return fileName.substring(0, fileName.length() - 1);
      } else {
        return fileName;
      }
    }
  }

  public static String getFileNameExtension(final Path path) {
    final String fileName = getFileName(path);
    return FileUtil.getFileNameExtension(fileName);
  }

  public static List<String> getFileNameExtensions(final Path path) {
    final String fileName = getFileName(path);
    return FileUtil.getFileNameExtensions(fileName);
  }

  public static boolean isHidden(final Path path) {
    try {
      if (Files.exists(path)) {
        final Path root = path.getRoot();
        if (!root.equals(path)) {
          final BasicFileAttributes attributes = Files.readAttributes(path,
            BasicFileAttributes.class);
          if (attributes instanceof DosFileAttributes) {
            final DosFileAttributes dosAttributes = (DosFileAttributes)attributes;
            return dosAttributes.isHidden();
          } else {
            final File file = path.toFile();
            return file.isHidden();
          }
        }
      }
    } catch (final Throwable e) {
      return false;
    }
    return false;
  }

  public static OutputStream outputStream(final Path path) {
    try {
      return Files.newOutputStream(path);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
