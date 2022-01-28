package com.revolsys.http;

import java.util.function.Function;

import com.revolsys.net.oauth.BearerToken;

public class BearerTokenRequestBuilderFactory extends ApacheHttpRequestBuilderFactory {

  private final Function<BearerToken, BearerToken> tokenRefesh;

  private BearerToken token;

  public BearerTokenRequestBuilderFactory(final Function<BearerToken, BearerToken> tokenRefesh) {
    this.tokenRefesh = tokenRefesh;
  }

  protected String getAccessToken() {
    if (this.token == null || this.token.isExpired()) {
      this.token = this.tokenRefesh.apply(this.token);
    }
    if (this.token == null) {
      return null;
    } else {
      return this.token.getAccessToken();
    }
  }

  protected String getAuthorizationHeader() {
    final String accessToken = getAccessToken();
    return "Bearer " + accessToken;
  }

  @Override
  protected ApacheHttpRequestBuilder newRequestBuilder() {
    return new BearerTokenRequestBuilder(this);
  }
}
