package com.revolsys.ui.html.builder;

public interface HtmlUiBuilderAware<T extends HtmlUiBuilder<?>> {
  void setHtmlUiBuilder(T uiBuilder);
}
