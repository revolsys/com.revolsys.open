package com.revolsys.http;

import com.revolsys.net.oauth.OpenIdConnectClient;

public class OpenIdRequestBuilderFactory extends BearerTokenRequestBuilderFactory {

  public OpenIdRequestBuilderFactory(final OpenIdConnectClient client, final String scope) {
    super(token -> client.tokenClientCredentials(scope));
  }

}
