package com.revolsys.http;

import com.revolsys.net.oauth.OpenIdBearerToken;

public class OpenIdBearerTokenRequestBuilderFactory
  extends BearerTokenRequestBuilderFactory<OpenIdBearerToken> {

  public OpenIdBearerTokenRequestBuilderFactory(final OpenIdBearerToken token) {
    super(token);
  }

  @Override
  protected OpenIdBearerToken tokenRefresh(final OpenIdBearerToken token) {
    if (token != null) {
      return token.refreshToken();
    }
    return null;
  }
}
