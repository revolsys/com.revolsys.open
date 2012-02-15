package com.revolsys.spring;

import java.io.IOException;

import org.springframework.core.io.Resource;

public class ByteArrayResource extends
  org.springframework.core.io.ByteArrayResource {

  private final String filename;

  public ByteArrayResource(final String filename, final byte[] data) {
    super(data);
    this.filename = filename;
  }

  public ByteArrayResource(final String filename, final byte[] data,
    final String description) {
    super(data, description);
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
