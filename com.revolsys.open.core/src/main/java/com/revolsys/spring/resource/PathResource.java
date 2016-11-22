/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.spring.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;

import com.revolsys.io.PathUtil;
import com.revolsys.util.WrappedException;

/**
 * {@link Resource} implementation for {@code com.revolsys.nio.file.Path} handles.
 * Supports resolution as File, and also as URL.
 * Implements the extended {@link WritableResource} interface.
 *
 * @author Philippe Marschall
 * @since 4.0
 * @see java.nio.file.Path
 */
public class PathResource extends AbstractResource implements WritableResource {
  private final Path path;

  /**
   * Construct a new new PathResource from a PathUtil handle.
   * <p>Note: Unlike {@link FileSystemResource}, when building relative resources
   * via {@link #createRelative}, the relative path will be built <i>underneath</i>
   * the given root:
   * e.g. Paths.get("C:/dir1/"), relative path "dir2" -> "C:/dir1/dir2"!
   * @param path a PathUtil handle
   */
  public PathResource(final Path path) {
    Assert.notNull(path, "Path must not be null");
    this.path = path.normalize();
  }

  /**
   * Construct a new new PathResource from a PathUtil handle.
   * <p>Note: Unlike {@link FileSystemResource}, when building relative resources
   * via {@link #createRelative}, the relative path will be built <i>underneath</i>
   * the given root:
   * e.g. Paths.get("C:/dir1/"), relative path "dir2" -> "C:/dir1/dir2"!
   * @param path a path
   * @see com.revolsys.io.file.com.revolsys.nio.file.Paths#getPath(String, String...)
   */
  public PathResource(final String path) {
    Assert.notNull(path, "Path must not be null");
    this.path = Paths.get(path).normalize();
  }

  /**
   * Construct a new new PathResource from a PathUtil handle.
   * <p>Note: Unlike {@link FileSystemResource}, when building relative resources
   * via {@link #createRelative}, the relative path will be built <i>underneath</i>
   * the given root:
   * e.g. Paths.get("C:/dir1/"), relative path "dir2" -> "C:/dir1/dir2"!
   * @see com.revolsys.io.file.com.revolsys.nio.file.Paths#get(URI)
   * @param uri a path URI
   */
  public PathResource(final URI uri) {
    Assert.notNull(uri, "URI must not be null");
    this.path = Paths.get(uri).normalize();
  }

  /**
   * This implementation returns the underlying File's length.
   */
  @Override
  public long contentLength() throws IOException {
    return Files.size(this.path);
  }

  @Override
  public void copyFrom(final InputStream in) {
    final Path path = getPath();
    final Path parent = path.getParent();
    if (!Files.exists(parent)) {
      try {
        Files.createDirectories(parent);
      } catch (final IOException e) {
        throw new WrappedException(e);
      }
    }
    super.copyFrom(in);
  }

  @Override
  public boolean createParentDirectories() {
    com.revolsys.io.file.Paths.createParentDirectories(this.path);
    return true;
  }

  /**
   * This implementation creates a FileResource, applying the given path
   * relative to the path of the underlying file of this resource descriptor.
   * @see java.nio.file.Path#resolve(String)
   */
  @Override
  public Resource createRelative(final String relativePath) {
    final Path childPath = this.path.resolve(relativePath);
    return new PathResource(childPath);
  }

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(this.path);
    } catch (final DirectoryNotEmptyException e) {
      return false;
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  /**
   * This implementation compares the underlying PathUtil references.
   */
  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof PathResource && this.path.equals(((PathResource)obj).path);
  }

  /**
   * This implementation returns whether the underlying file exists.
   * @see PathResource#exists()
   */
  @Override
  public boolean exists() {
    return Files.exists(this.path);
  }

  @Override
  public String getDescription() {
    return this.path.toAbsolutePath().toString();
  }

  /**
   * This implementation returns the underlying File reference.
   */
  @Override
  public File getFile() {
    return this.path.toFile();
  }

  /**
   * This implementation returns the name of the file.
   * @see java.nio.file.Path#getFileName()
   */
  @Override
  public String getFilename() {
    return this.path.getFileName().toString();
  }

  /**
   * This implementation opens a InputStream for the underlying file.
   * @see com.revolsys.nio.file.spi.FileSystemProvider#newInputStream(PathUtil, OpenOption...)
   */
  @Override
  public InputStream getInputStream() {
    if (!exists()) {
      throw new IllegalArgumentException(getPath() + " (no such file or directory)");
    }
    if (Files.isDirectory(this.path)) {
      throw new IllegalArgumentException(getPath() + " (is a directory)");
    }
    try {
      return Files.newInputStream(this.path);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      final Path path = getPath();
      return Files.newOutputStream(path, com.revolsys.io.file.Paths.OPEN_OPTIONS_NONE);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  @Override
  public Resource getParent() {
    final Path parentPath = this.path.getParent();
    if (parentPath == null) {
      return null;
    } else {
      return new PathResource(parentPath);
    }
  }

  /**
   * Return the file path for this resource.
   */
  public final Path getPath() {
    return this.path;
  }

  /**
   * This implementation returns a URI for the underlying file.
   * @see java.nio.file.Path#toUri()
   */
  @Override
  public URI getURI() throws IOException {
    return this.path.toUri();
  }

  /**
   * This implementation returns a URL for the underlying file.
   * @see java.nio.file.Path#toUri()
   * @see java.net.URI#toURL()
   */
  @Override
  public URL getURL() {
    try {
      return this.path.toUri().toURL();
    } catch (final MalformedURLException e) {
      throw new WrappedException(e);
    }
  }

  /**
   * This implementation returns the hash code of the underlying PathUtil reference.
   */
  @Override
  public int hashCode() {
    return this.path.hashCode();
  }

  // implementation of WritableResource

  /**
   * This implementation checks whether the underlying file is marked as readable
   * (and corresponds to an actual file with content, not to a directory).
   * @see com.revolsys.nio.file.Files#isReadable(PathUtil)
   * @see com.revolsys.nio.file.Files#isDirectory(PathUtil, com.revolsys.nio.file.LinkOption...)
   */
  @Override
  public boolean isReadable() {
    return Files.isReadable(this.path) && !Files.isDirectory(this.path);
  }

  @Override
  public boolean isWritable() {
    return Files.isWritable(this.path) && !Files.isDirectory(this.path);
  }

  /**
   * This implementation returns the underlying File's timestamp.
   * @see com.revolsys.nio.file.Files#getLastModifiedTime(PathUtil, com.revolsys.nio.file.LinkOption...)
   */
  @Override
  public long lastModified() throws IOException {
    // we can not use the super class method since it uses conversion to a File
    // and
    // only Paths on the default file system can be converted to a File
    return Files.getLastModifiedTime(this.path).toMillis();
  }

  @Override
  public OutputStream newOutputStream() {
    return getOutputStream();
  }

}
