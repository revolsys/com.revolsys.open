package com.revolsys.web.security.oauth;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuthLoginState {

  private final String originalRequestUri;

  private final String nonce = UUID.randomUUID().toString().replace("-", "-");

  private final String stateId = UUID.randomUUID().toString().replace("-", "-");

  private final MicrosoftOAuthHandler handler;

  public OAuthLoginState(final MicrosoftOAuthHandler handler, final String originalRequestUri) {
    this.handler = handler;
    this.originalRequestUri = originalRequestUri;
  }

  public String authCallback(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    return this.handler.callback(this, request, response);
  }

  public String authorize(final HttpServletRequest request) throws IOException {
    return this.handler.authorize(request, this);
  }

  public boolean equalsNonce(final String tokenNonce) {
    return this.nonce.equals(tokenNonce);
  }

  public String getNonce() {
    return this.nonce;
  }

  public String getOriginalRequestUri() {
    return this.originalRequestUri;
  }

  public String getStateKey() {
    return "state-" + this.stateId;
  }

  public String getStateParam() {
    return this.stateId;
  }

  @Override
  public String toString() {
    return this.originalRequestUri;
  }
}
