package com.revolsys.http;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class SasToken {

  private List<NameValuePair> params = new ArrayList<>();

  private Instant expiry;

  public SasToken(String token) {
    if (token.startsWith("?")) {
      token = token.substring(1);
    }
    this.params = URLEncodedUtils.parse(token, StandardCharsets.UTF_8);
    for (final NameValuePair param : this.params) {
      if (param.getName().equals("se")) {
        final String expiryString = param.getValue();
        this.expiry = Instant.parse(expiryString);
      }
    }
  }

  public void applyTo(final ApacheHttpRequestBuilder requestBuilder) {
    for (final NameValuePair param : this.params) {
      requestBuilder.setParameter(param);
    }
  }

  public boolean isExpired() {
    return this.expiry.isAfter(Instant.now());
  }
}
