package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ServerOverrideFilter implements Filter {

  private String serverUrl;

  public void destroy() {
  }

  public void doFilter(final ServletRequest req,
    final ServletResponse response, final FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest)req;
    request = new ServerOverrideHttpServletRequest(serverUrl, request);
    chain.doFilter(request, response);
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  public void setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
  }

}
