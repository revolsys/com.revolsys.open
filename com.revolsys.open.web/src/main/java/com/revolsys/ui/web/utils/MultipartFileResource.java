package com.revolsys.ui.web.utils;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileResource extends AbstractResource {

  private final MultipartFile file;

  public MultipartFileResource(final MultipartFile file) {
    this.file = file;
  }

  @Override
  public long contentLength() throws IOException {
    return this.file.getSize();
  }

  @Override
  public String getDescription() {
    return this.file.getName();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return this.file.getInputStream();
  }
}
