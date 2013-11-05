package com.revolsys.converter.string;

import java.net.URL;

import com.revolsys.util.UrlUtil;

public class UrlStringConverter implements StringConverter<URL> {
  public UrlStringConverter() {
  }

  @Override
  public Class<URL> getConvertedClass() {
    return URL.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public URL toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof URL) {
      final URL url = (URL)value;
      return url;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public URL toObject(final String string) {
    return UrlUtil.getUrl(string);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final URL url = toObject(value);
      return url.toString();
    }
  }
}
