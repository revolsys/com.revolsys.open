package com.revolsys.spring.resource;

import java.io.File;
import java.net.URL;

public abstract class AbstractResource extends org.springframework.core.io.AbstractResource
  implements Resource {

  @Override
  public Resource createRelative(final String relativePath) {
    throw new UnsupportedOperationException("Cannot create relative resource for: " + relativePath);
  }

  /**
   * This implementation throws a FileNotFoundException, assuming
   * that the resource cannot be resolved to an absolute file path.
   */
  @Override
  public File getFile() {
    throw new UnsupportedOperationException(
      getDescription() + " cannot be resolved to absolute file path");
  }

  @Override
  public URL getURL() {
    throw new UnsupportedOperationException(getDescription() + " cannot be resolved to URL");
  }

}
