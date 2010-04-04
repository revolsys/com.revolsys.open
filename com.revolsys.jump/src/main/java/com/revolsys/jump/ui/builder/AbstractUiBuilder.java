package com.revolsys.jump.ui.builder;

public abstract class AbstractUiBuilder implements UiBuilder {
  private UiBuilderRegistry registry;

  public String toHtml(final Object object) {
    StringBuffer s = new StringBuffer();
    appendHtml(s, object);
    return s.toString();
  }

  /**
   * @return the registry
   */
  public UiBuilderRegistry getRegistry() {
    return registry;
  }

  /**
   * @param registry the registry to set
   */
  public void setRegistry(final UiBuilderRegistry registry) {
    this.registry = registry;
  }

}
