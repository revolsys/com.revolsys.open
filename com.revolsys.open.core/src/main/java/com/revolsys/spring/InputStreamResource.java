package com.revolsys.spring;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

public class InputStreamResource extends
  org.springframework.core.io.InputStreamResource {

  private final String filename;

  private long length = -1;

  public InputStreamResource(final String filename,
    final InputStream inputStream) {
    super(inputStream);
    this.filename = filename;
  }

  public InputStreamResource(final String filename,
    final InputStream inputStream, final long length) {
    super(inputStream);
    this.filename = filename;
    this.length = length;
  }

  public InputStreamResource(final String filename,
    final InputStream inputStream, final String description) {
    super(inputStream, description);
    this.filename = filename;
  }

  @Override
  public long contentLength() throws IOException {
    if (length >= 0) {
      return length;
    } else {
      return super.contentLength();
    }
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
