package com.revolsys.net.oauth;

import java.text.ParseException;
import java.time.Instant;

import org.jeometry.common.exception.Exceptions;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.revolsys.record.io.format.json.JsonObject;

public class BearerToken {
  private final int expiresIn;

  private final String accessToken;

  private final long expireTime;

  private final String refreshToken;

  private final String scope;

  private final String returnedScope;

  private final String idToken;

  private final OpenIdConnectClient client;

  private JWT jwt;

  public BearerToken(final OpenIdConnectClient client, final JsonObject config,
    final String scope) {
    this.client = client;
    this.expiresIn = config.getInteger("expires_in");
    this.returnedScope = config.getString("scope");
    if (scope == null) {
      this.scope = this.returnedScope;
    } else {
      this.scope = scope;
    }
    this.accessToken = config.getString("access_token");
    this.refreshToken = config.getString("refresh_token");
    this.idToken = config.getString("id_token");
    this.expireTime = System.currentTimeMillis() + this.expiresIn * 1000;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public OpenIdConnectClient getClient() {
    return this.client;
  }

  public int getExpiresIn() {
    return this.expiresIn;
  }

  public Instant getExpireTime() {
    return Instant.ofEpochMilli(this.expireTime);
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

  public String getReturnedScope() {
    return this.returnedScope;
  }

  public String getScope() {
    return this.scope;
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

  public BearerToken getValid() {
    if (isExpired()) {
      return refreshToken();
    } else {
      return this;
    }
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= this.expireTime;
  }

  public BearerToken refreshToken() {
    if (this.refreshToken == null) {
      return null;
    } else {
      return this.client.tokenRefresh(this.refreshToken, this.scope);
    }
  }

  @Override
  public String toString() {
    return this.accessToken;
  }

}
