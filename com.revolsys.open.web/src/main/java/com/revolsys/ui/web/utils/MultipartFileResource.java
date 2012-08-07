package com.revolsys.ui.web.utils;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileResource extends AbstractResource {

  private MultipartFile file;

  public MultipartFileResource(MultipartFile file) {
    this.file = file;
  }

  @Override
  public String getDescription() {
    return file.getName();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return file.getInputStream();
  }

  @Override
  public long contentLength() throws IOException {
    return file.getSize();
  }
}
