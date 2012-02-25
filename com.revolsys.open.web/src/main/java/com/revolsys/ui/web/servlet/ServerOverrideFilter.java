package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerOverrideFilter extends SavedRequestFilter {

  private String serverUrl;

  public void destroy() {
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequest overrideRequest = new ServerOverrideHttpServletRequest(
      serverUrl, request);
    super.doFilterInternal(overrideRequest, response, filterChain);
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
  }

}
