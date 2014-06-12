package com.revolsys.gdal;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.revolsys.io.IoFactory;

public class GdalIoFactory implements IoFactory {
  static {
    Gdal.init();
  }

  @Override
  public String getFileExtension(final String mediaType) {
    return null;
  }

  @Override
  public List<String> getFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public String getMediaType(final String fileExtension) {
    return null;
  }

  @Override
  public Set<String> getMediaTypes() {
    return Collections.emptySet();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean isAvailable() {
    return false;
  }

}
