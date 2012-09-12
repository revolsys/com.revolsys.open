package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

public class OutputStreamResource extends AbstractResource {

  private final String filename;

  private final OutputStream outputStream;

  private String description;

  private boolean read;

  public OutputStreamResource(final String filename,
    final OutputStream outputStream) {
    this.outputStream = outputStream;
    this.filename = filename;
  }

  public OutputStreamResource(final String filename,
    final OutputStream outputStream, final String description) {
    this.filename = filename;
    this.outputStream = outputStream;
    this.description = description;
  }

  @Override
  public Resource createRelative(final String relativePath) throws IOException {
    return new NonExistingResource();
  }

  @Override
  public boolean equals(final Object object) {
    return (object == this);
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return filename;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    throw new IllegalArgumentException("No input stream exists");
  }

  public OutputStream getOutputStream() {
    if (read) {
      throw new IllegalStateException(
        "OutputStream has already been read - "
          + "do not use OutputStreamResource if a stream needs to be read multiple times");
    }
    read = true;
    return outputStream;
  }

  @Override
  public int hashCode() {
    return outputStream.hashCode();
  }

  @Override
  public boolean isOpen() {
    return true;
  }
}
