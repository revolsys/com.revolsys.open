package com.revolsys.http;

import com.revolsys.net.oauth.OpenIdBearerToken;
import com.revolsys.net.oauth.OpenIdConnectClient;

public class OpenIdConnectRequestBuilderFactory
  extends BearerTokenRequestBuilderFactory<OpenIdBearerToken> {

  private final OpenIdConnectClient client;

  private final String scope;

  public OpenIdConnectRequestBuilderFactory(final OpenIdConnectClient client, final String scope) {
    this.client = client;
    this.scope = scope;
  }

  @Override
  protected OpenIdBearerToken tokenRefresh(OpenIdBearerToken token) {
    if (token != null) {
      token = token.refreshToken();
      if (token != null) {
        return token;
      }
    }
    return this.client.tokenClientCredentials(this.scope);
  }
}
