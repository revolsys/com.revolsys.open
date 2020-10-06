package com.revolsys.spring.resource;

import java.io.File;
import java.net.URL;

import com.revolsys.util.UrlUtil;

public abstract class AbstractResource extends org.springframework.core.io.AbstractResource
  implements Resource {

  private Resource parent;

  public AbstractResource() {
  }

  public AbstractResource(final Resource parent) {
    this.parent = parent;
  }

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
  public Resource getParent() {
    return this.parent;
  }

  @Override
  public URL getURL() {
    throw new UnsupportedOperationException(getDescription() + " cannot be resolved to URL");
  }

  protected void setParent(final Resource parent) {
    this.parent = parent;
  }

}
