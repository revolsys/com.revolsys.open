package com.revolsys.http;

import com.revolsys.net.oauth.BearerToken;

public abstract class BearerTokenRequestBuilderFactory<T extends BearerToken>
  extends ApacheHttpRequestBuilderFactory {

  protected T token;

  public BearerTokenRequestBuilderFactory() {
  }

  public BearerTokenRequestBuilderFactory(final T token) {
    this.token = token;
  }

  protected synchronized String getAccessToken() {
    if (this.token == null || this.token.isExpired()) {
      this.token = tokenRefresh(this.token);
    }
    if (this.token == null) {
      return null;
    } else {
      return this.token.getAccessToken();
    }
  }

  public String getAuthorizationHeader() {
    final String accessToken = getAccessToken();
    return "Bearer " + accessToken;
  }

  @Override
  public ApacheHttpRequestBuilder newRequestBuilder() {
    return new BearerTokenRequestBuilder(this);
  }

  protected abstract T tokenRefresh(T token);
}
