package com.revolsys.converter.string;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.revolsys.util.UrlUtil;
import com.revolsys.util.WrappedException;

public class UriStringConverter implements StringConverter<URI> {
  public UriStringConverter() {
  }

  @Override
  public Class<URI> getConvertedClass() {
    return URI.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public URI objectToObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof URI) {
      final URI uri = (URI)value;
      return uri;
    } else if (value instanceof URL) {
      final URL url = (URL)value;
      try {
        return url.toURI();
      } catch (final URISyntaxException e) {
        throw new WrappedException(e);
      }
    } else {
      return stringToObject(value.toString());
    }
  }

  @Override
  public URI stringToObject(final String string) {
    return UrlUtil.getUri(string);
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final URI url = objectToObject(value);
      return url.toString();
    }
  }
}
