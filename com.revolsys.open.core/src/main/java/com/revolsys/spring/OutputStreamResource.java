package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

public class OutputStreamResource extends AbstractResource {

  private final String filename;

  private OutputStream outputStream;

  private String description;

  private boolean read;

  public OutputStreamResource(String filename, OutputStream outputStream,
    String description) {
    this.filename = filename;
    this.outputStream = outputStream;
    this.description = description;
  }

  public OutputStreamResource(String filename, OutputStream outputStream) {
    this.outputStream = outputStream;
    this.filename = filename;
  }

  @Override
  public boolean exists() {
    return true;
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
  public boolean isOpen() {
    return true;
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return filename;
  }

  @Override
  public Resource createRelative(String relativePath) throws IOException {
    return new NonExistingResource();
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public boolean equals(Object object) {
    return (object == this);
  }

  @Override
  public int hashCode() {
    return outputStream.hashCode();
  }

  public InputStream getInputStream() throws IOException {
    throw new IllegalArgumentException("No input stream exists");
  }
}
