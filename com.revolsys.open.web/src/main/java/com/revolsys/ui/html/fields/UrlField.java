package com.revolsys.ui.html.fields;

public class UrlField extends TextField {
  public UrlField(final String name, final boolean required) {
    this(name, required, null);
  }

  public UrlField(final String name, final boolean required, final Object defaultValue) {
    super(name, 60, 1000, defaultValue, required);
    setCssClass("url");
    setType("url");
  }

}
