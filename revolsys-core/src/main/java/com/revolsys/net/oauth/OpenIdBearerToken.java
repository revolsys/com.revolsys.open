package com.revolsys.net.oauth;

import java.text.ParseException;

import org.jeometry.common.exception.Exceptions;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.revolsys.record.io.format.json.JsonObject;

public class OpenIdBearerToken extends BearerToken {

  private JWT jwt;

  private final String refreshToken;

  private final String idToken;

  private final OpenIdConnectClient client;

  public OpenIdBearerToken(final OpenIdConnectClient client, final JsonObject config,
    final OpenIdResource resource) {
    super(config, resource.getResource());
    this.client = client;
    this.refreshToken = config.getString("refresh_token");
    this.idToken = config.getString("id_token");
    final Integer expiresIn = config.getInteger("expires_in");
    final long expireTime = System.currentTimeMillis() + expiresIn * 1000;
    setExpireTime(expireTime);
    final String returnedScope = config.getString("scope");
    setScope(resource.getResource(), returnedScope);
  }

  public OpenIdBearerToken(final OpenIdConnectClient client, final JsonObject config,
    final String scope) {
    super(config, scope);
    this.client = client;
    this.refreshToken = config.getString("refresh_token");
    this.idToken = config.getString("id_token");
    final Integer expiresIn = config.getInteger("expires_in");
    final long expireTime = System.currentTimeMillis() + expiresIn * 1000;
    setExpireTime(expireTime);
    final String returnedScope = config.getString("scope");
    setScope(scope, returnedScope);
  }

  public OpenIdConnectClient getClient() {
    return this.client;
  }

  public String getIdToken() {
    return this.idToken;
  }

  protected JWT getJwt() throws ParseException {
    if (this.jwt == null) {
      this.jwt = JWTParser.parse(this.idToken);
    }
    return this.jwt;
  }

  public String getRefreshToken() {
    return this.refreshToken;
  }

  public String getStringClaim(final String name) {
    try {
      final JWT jwt = getJwt();
      final JWTClaimsSet claims = jwt.getJWTClaimsSet();
      return claims.getStringClaim(name);
    } catch (final ParseException e) {
      throw Exceptions.wrap("idToken invalid", e);
    }
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
