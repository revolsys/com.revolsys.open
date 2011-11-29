package com.revolsys.spring;

import java.io.IOException;

import org.springframework.core.io.Resource;

public class ByteArrayResource extends
  org.springframework.core.io.ByteArrayResource {

  private final String filename;

  public ByteArrayResource(String filename, byte[] data, String description) {
    super(data, description);
    this.filename = filename;
  }

  public ByteArrayResource(String filename, byte[] data) {
    super(data);
    this.filename = filename;
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return filename;
  }

  @Override
  public Resource createRelative(String relativePath) throws IOException {
    return new NonExistingResource();
  }
}
