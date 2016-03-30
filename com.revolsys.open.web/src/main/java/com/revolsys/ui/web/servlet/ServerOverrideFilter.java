package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerOverrideFilter extends SavedRequestFilter {

  private String serverUrl;

  @Override
  public void destroy() {
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
    final HttpServletResponse response, final FilterChain filterChain)
    throws ServletException, IOException {
    if (request.getCharacterEncoding() == null) {
      request.setCharacterEncoding("UTF-8");
    }
    final HttpServletRequest overrideRequest = new ServerOverrideHttpServletRequest(this.serverUrl,
      request);
    super.doFilterInternal(overrideRequest, response, filterChain);
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  public void setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
  }

}
