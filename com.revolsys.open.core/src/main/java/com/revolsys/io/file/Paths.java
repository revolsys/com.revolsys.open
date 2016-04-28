package com.revolsys.io.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;

import com.revolsys.io.FileNames;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;

public interface Paths {
  static void createDirectories(final Path path) {
    if (!Paths.exists(path)) {
      try {
        Files.createDirectories(path);
      } catch (final IOException e) {
        throw new WrappedException(e);
      }
    }
  }

  static void createParentDirectories(final Path path) {
    if (!Paths.exists(path)) {
      try {
        final Path parent = path.getParent();
        Files.createDirectories(parent);
      } catch (final IOException e) {
        throw new WrappedException(e);
      }
    }
  }

  static boolean deleteDirectories(final Path path) {
    if (Paths.exists(path)) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

          @Override
          public FileVisitResult postVisitDirectory(final Path dir, final IOException exception)
            throws IOException {

            if (exception == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw exception;
            }
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
            throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
        });
        return true;
      } catch (final IOException e) {
        return false;
      }
    }
    return true;
  }

  static boolean exists(final Path path) {
    return Files.exists(path);
  }

  static Path get(final File file) {
    if (file != null) {
      final File parentFile = file.getParentFile();
      parentFile.mkdirs();
      return file.toPath();
    }
    return null;
  }

  static Path get(final String first, final String... more) {
    return java.nio.file.Paths.get(first, more);
  }

  static String getBaseName(final java.nio.file.Path path) {
    final String fileName = getFileName(path);
    return FileNames.getBaseName(fileName);
  }

  static Path getDirectoryPath(final Path path) {
    createDirectories(path);
    return getPath(path);
  }

  static Path getDirectoryPath(final Path parent, final String path) {
    final Path childPath = parent.resolve(path);
    return getDirectoryPath(childPath);
  }

  static String getFileName(final Path path) {
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

  static String getFileNameExtension(final Path path) {
    final String fileName = getFileName(path);
    return FileNames.getFileNameExtension(fileName);
  }

  static List<String> getFileNameExtensions(final Path path) {
    final String fileName = getFileName(path);
    return FileNames.getFileNameExtensions(fileName);
  }

  static Path getPath(final Path path) {
    return path.toAbsolutePath();
  }

  static Path getPath(final Path parent, final String path) {
    final Path childPath = parent.resolve(path);
    return getPath(childPath);
  }

  static Path getPath(final String name) {
    if (Property.hasValue(name)) {
      final Path path = Paths.get(name);
      return getPath(path);
    } else {
      return null;
    }
  }

  static Path getPath(final URI uri) {
    if (uri != null) {
      final Path path = java.nio.file.Paths.get(uri);
      return getPath(path);
    } else {
      return null;
    }
  }

  static boolean isHidden(final Path path) {
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

  static FileTime lastModified(final Path path) {
    try {
      return Files.getLastModifiedTime(path);
    } catch (final IOException e) {
      return FileTime.fromMillis(0);
    }
  }

  static Writer newWriter(final Path path) {
    try {
      return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  static OutputStream outputStream(final Path path) {
    try {
      return Files.newOutputStream(path);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  static String toPathString(final Path path) {
    return getPath(path).toString();
  }

  static URL toUrl(final Path path) {
    try {
      return path.toUri().toURL();
    } catch (final MalformedURLException e) {
      throw new WrappedException(e);
    }
  }

  static String toUrlString(final Path path) {
    return toUrl(path).toString();
  }

  static Path withExtension(final Path path, final String extension) {
    final String baseName = getBaseName(path);
    final String newFileName = baseName + "." + extension;
    final Path parent = path.getParent();
    return parent.resolve(newFileName);
  }
}
