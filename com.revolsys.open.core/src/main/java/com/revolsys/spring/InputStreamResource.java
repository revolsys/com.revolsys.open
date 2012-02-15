package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

public class InputStreamResource extends
  org.springframework.core.io.InputStreamResource {

  private final String filename;

  public InputStreamResource(final String filename,
    final InputStream inputStream) {
    super(inputStream);
    this.filename = filename;
  }

  public InputStreamResource(final String filename,
    final InputStream inputStream, final String description) {
    super(inputStream, description);
    this.filename = filename;
  }

  @Override
  public Resource createRelative(final String relativePath) throws IOException {
    return new NonExistingResource();
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return filename;
  }
}
