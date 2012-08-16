package com.revolsys.ui.html.fields;

public class UrlField extends TextField {
  public UrlField(final String name, final boolean required) {
    super(name, 40, 1000, required);
  }

  public UrlField(final String name,
    final boolean required, final Object defaultValue) {
    super(name, 40, 1000, defaultValue, required);
  }

}
