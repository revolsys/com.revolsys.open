package com.revolsys.spring.resource;

public abstract class AbstractResource extends org.springframework.core.io.AbstractResource
  implements Resource {

  @Override
  public Resource createRelative(final String relativePath) {
    throw new UnsupportedOperationException("Cannot create relative resource for: " + relativePath);
  }
}
