package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

public class InputStreamResource extends
  org.springframework.core.io.InputStreamResource {

  private final String filename;

  public InputStreamResource(
    String filename,
    InputStream inputStream,
    String description) {
    super(inputStream, description);
    this.filename = filename;
  }

  public InputStreamResource(
    String filename,
    InputStream inputStream) {
    super(inputStream);
    this.filename = filename;
  }

  @Override
  public String getFilename()
    throws IllegalStateException {
    return filename;
  }

  @Override
  public Resource createRelative(
    String relativePath)
    throws IOException {
    return new NonExistingResource();
  }
}
