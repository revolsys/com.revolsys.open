package com.revolsys.swing.map.symbolizer;

public class ExternalGraphicSymbol extends AbstractGraphicSymbol {
  private CharSequence url;

  private CharSequence format;

  public ExternalGraphicSymbol(final CharSequence url, final CharSequence format) {
    this.url = url;
    this.format = format;
  }

  public CharSequence getFormat() {
    return format;
  }

  public CharSequence getUrl() {
    return url;
  }

  public void setFormat(final CharSequence format) {
    this.format = format;
  }

  public void setUrl(final CharSequence url) {
    this.url = url;
  }

}
