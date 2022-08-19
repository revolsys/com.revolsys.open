package com.revolsys.net.oauth;

import com.revolsys.record.io.format.json.JsonObject;

public class OpenIdBearerToken extends BearerToken {

  private JsonWebToken jwt;

  private final String refreshToken;

  private final String idToken;

  private final OpenIdConnectClient client;

  public OpenIdBearerToken(final OpenIdConnectClient client, final JsonObject config,
    final OpenIdResource resource) {
    super(config, resource.getResource());
    this.client = client;
    this.refreshToken = config.getString("refresh_token");
    this.idToken = config.getString("id_token");
    final String returnedScope = config.getString("scope");
    setScope(resource.getResource(), returnedScope);
  }

  public OpenIdBearerToken(final OpenIdConnectClient client, final JsonObject config,
    final String scope) {
    super(config, scope);
    this.client = client;
    this.refreshToken = config.getString("refresh_token");
    this.idToken = config.getString("id_token");
    final String returnedScope = config.getString("scope");
    setScope(scope, returnedScope);
  }

  public OpenIdConnectClient getClient() {
    return this.client;
  }

  public String getIdToken() {
    return this.idToken;
  }

  protected JsonWebToken getJwt() {
    if (this.jwt == null && this.idToken != null) {
      this.jwt = new JsonWebToken(this.idToken);
    }
    return this.jwt;
  }

  public String getRefreshToken() {
    return this.refreshToken;
  }

  public String getStringClaim(final String name) {
    final JsonWebToken jwt = getJwt();
    if (jwt == null) {
      return null;
    }
    return jwt.getString(name);
  }

  public OpenIdBearerToken getValid() {
    if (isExpired()) {
      return refreshToken();
    } else {
      return this;
    }
  }

  public OpenIdBearerToken refreshToken() {
    if (this.refreshToken == null || this.client == null) {
      return null;
    } else {
      final String scope = getScope();
      return this.client.tokenRefresh(this.refreshToken, scope);
    }
  }
}
