package com.revolsys.ui.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServerOverrideHttpServletRequest extends HttpServletRequestWrapper {
  private String serverUrl;

  public ServerOverrideHttpServletRequest(final String serverUrl,
    HttpServletRequest request) {
    super(request);
    this.serverUrl = serverUrl;
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer url = new StringBuffer(serverUrl);
    String contextPath = getContextPath();
    if (contextPath != null) {
      url.append(contextPath);
    }
    String servletPath = getServletPath();
    if (servletPath != null) {
      url.append(servletPath);
    }
    String pathInfo = getPathInfo();
    if (pathInfo != null) {
      url.append(pathInfo);
    }
    return url;
  }
}
