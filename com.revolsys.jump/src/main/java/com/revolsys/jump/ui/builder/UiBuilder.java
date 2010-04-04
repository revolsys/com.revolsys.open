package com.revolsys.jump.ui.builder;

public interface UiBuilder {
  void appendHtml(StringBuffer s, Object object);

  String toHtml(Object object);

  UiBuilderRegistry getRegistry();

  void setRegistry(UiBuilderRegistry registry);

}
