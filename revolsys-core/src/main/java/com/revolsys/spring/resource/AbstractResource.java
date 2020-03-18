package com.revolsys.spring.resource;

import java.io.File;
import java.net.URL;

import com.revolsys.util.UrlUtil;

public abstract class AbstractResource extends org.springframework.core.io.AbstractResource
  implements Resource {

  @Override
  public Resource createRelative(final String relativePath) {
    throw new UnsupportedOperationException("Cannot create relative resource for: " + relativePath);
  }

  @Override
  public File getFile() {
    final URL url = getURL();
    return UrlUtil.toFile(url);
  }

  @Override
  public URL getURL() {
    throw new UnsupportedOperationException(getDescription() + " cannot be resolved to URL");
  }

}
