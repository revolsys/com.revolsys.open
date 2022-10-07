package com.revolsys.web.security.oauth;

import java.io.IOException;
import java.security.Principal;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.methods.RequestBuilder;
import org.jeometry.common.logging.Logs;

import com.revolsys.net.oauth.OpenIdBearerToken;
import com.revolsys.net.oauth.OpenIdConnectClient;
import com.revolsys.web.HttpServletUtils;

public class MicrosoftOAuthHandler {

  private final OpenIdConnectClient oauthClient;

  private final String scope = "openid offline_access profile email";

  private final String prompt = "select_account";

  private final Function<OpenIdBearerToken, Principal> principalFactory;

  public MicrosoftOAuthHandler(final String clientId, final String clientSecret,
    final Function<OpenIdBearerToken, Principal> principalFactory) {
    this.oauthClient = OpenIdConnectClient//
      .microsoftCommon()
      .setClientId(clientId)
      .setClientSecret(clientSecret);
    this.principalFactory = principalFactory;
  }

  public String authorize(final HttpServletRequest request, final OAuthLoginState loginState)
    throws IOException {
    final String redirectUri = getOAuthRedirectUri(request);
    final String state = loginState.getStateParam();
    final String nonce = loginState.getNonce();
    return getRedirectUri(redirectUri, state, nonce, this.scope);
  }

  public OpenIdBearerToken callback(final HttpServletRequest request, final String redirectUri,
    final String nonce, String scope) {
    final String code = request.getParameter("code");
    final OpenIdBearerToken token = this.oauthClient.tokenAuthorizationCode(code, redirectUri,
      scope);

    final String tokenNonce = token.getStringClaim("nonce");
    if (!nonce.equals(tokenNonce)) {
      throw new IllegalStateException(
        "Failed to validate data received from Authorization service - could not validate nonce");
    }
    return token;
  }

  public String callback(final OAuthLoginState loginState, final HttpServletRequest request,
    final HttpServletResponse response) throws IOException {
    final HttpSession session = request.getSession();
    try {
      final String redirectUri = getOAuthRedirectUri(request);
      final String nonce = loginState.getNonce();
      final OpenIdBearerToken token = callback(request, redirectUri, nonce, this.scope);
      session.setAttribute("bearerToken", token);
      final Principal principal = this.principalFactory.apply(token);
      session.setAttribute("principal", principal);
    } catch (final IllegalStateException e) {
    } catch (final Exception e) {
      Logs.error(this, "Login Error", e);
    }
    return loginState.getOriginalRequestUri();
  }

  public String getOAuthRedirectUri(final HttpServletRequest request) {
    final String serverUrl = HttpServletUtils.getServerUrl(request);
    return serverUrl + "/auth/openid/return";
  }

  public String getRedirectUri(final String redirectUri, final String state, final String nonce, String scope) {
    final RequestBuilder urlBuilder = this.oauthClient.authorizationUrlBuilder(scope,
      redirectUri, state, nonce, this.prompt);
    return urlBuilder.build().getURI().toASCIIString();
  }

  public String getState(final HttpServletRequest request) {
    final String requestURI = request.getRequestURI();
    if (requestURI == null || requestURI.length() == 0) {
      return "/";
    } else {
      return requestURI;
    }
  }

  public boolean isValid(final HttpServletRequest request, final String requestUri)
    throws IOException {
    final HttpSession session = request.getSession();
    try {
      OpenIdBearerToken token = (OpenIdBearerToken)session.getAttribute("bearerToken");
      if (token != null) {
        if (token.isExpired()) {
          token = token.getValid();
          if (token != null) {
            session.setAttribute("bearerToken", token);
            return true;
          }
        } else {
          return true;
        }
      }
    } catch (final Exception e) {
      Logs.error(this, requestUri, e);
    }
    return false;
  }

  protected OpenIdConnectClient newClient(final String clientId) {
    throw new UnsupportedOperationException();
  }

  public void redirectToPath(final HttpServletRequest request, final HttpServletResponse response,
    final String path) throws IOException {
    final String serverUrl = HttpServletUtils.getServerUrl(request);
    String successRedirectUrl;
    if (path == null || path.length() == 0) {
      successRedirectUrl = serverUrl;
    } else {
      successRedirectUrl = serverUrl + path;
    }
    response.sendRedirect(successRedirectUrl);
  }

  public void signOff(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    request.getSession().invalidate();
    final String serverUrl = HttpServletUtils.getServerUrl(request);
    final String redirectUrl = this.oauthClient.endSessionUrl(serverUrl).toASCIIString();
    response.sendRedirect(redirectUrl);
  }

  public OpenIdBearerToken tokenRefresh(final String refreshToken, final String scope) {
    return this.oauthClient.tokenRefresh(refreshToken, scope);
  }
}
