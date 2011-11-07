package com.revolsys.ui.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultUriResolver implements UriResolver {
  private static final String URI_RE = "^\\p{Alpha}[\\w\\+\\.\\-]*:";

  private static final Pattern URI_PATTERN = Pattern.compile(URI_RE);

  public String resolveUri(final String uri) {
    Matcher matcher = URI_PATTERN.matcher(uri);
    if (matcher.find()) {
      return uri;
    } else {
      return resolveRelativeUri(uri);
    }
  }

  protected String resolveRelativeUri(final String uri) {
    return uri;
  }

}
