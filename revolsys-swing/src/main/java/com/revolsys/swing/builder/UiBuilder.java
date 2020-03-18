package com.revolsys.swing.builder;

public interface UiBuilder {
  void appendHtml(StringBuilder s, Object object);

  UiBuilderRegistry getRegistry();

  void setRegistry(UiBuilderRegistry registry);

  String toHtml(Object object);

}
