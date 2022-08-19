package com.revolsys.net.oauth;

import java.time.Instant;

import com.revolsys.record.io.format.json.JsonObject;

public class BearerToken {

  private final String accessToken;

  private long expireTime;

  private String scope;

  private String returnedScope;

  public BearerToken(final JsonObject config) {
    this(config, null);
  }

  public BearerToken(final JsonObject config, final String scope) {
    this.accessToken = config.getString("access_token");

    final Long expiresOn = config.getLong("expires_on");
    if (expiresOn != null) {
      this.expireTime = expiresOn * 1000;
    } else {
      final Long expiresIn = config.getLong("expires_in");
      if (expiresIn != null) {
        this.expireTime = (System.currentTimeMillis() + expiresIn) * 1000;
      }
    }

  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public String getAuthorizationHeader() {
    return "Bearer " + this.accessToken;
  }

  public Instant getExpireTime() {
    return Instant.ofEpochMilli(this.expireTime);
  }

  public long getExpireTimeMillis() {
    return this.expireTime;
  }

  public String getReturnedScope() {
    return this.returnedScope;
  }

  public String getScope() {
    return this.scope;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= this.expireTime;
  }

  public void setScope(final String scope, final String returnedScope) {
    this.returnedScope = returnedScope;
    if (scope == null) {
      this.scope = returnedScope;
    } else {
      this.scope = scope;
    }
  }

  @Override
  public String toString() {
    if (this.accessToken == null) {
      return "No Token";
    } else {
      try {
        return new JsonWebToken(this.accessToken).toString();
      } catch (final Exception e) {
        return this.accessToken;
      }
    }
  }

}
