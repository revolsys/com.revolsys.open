package com.revolsys.ui.web.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServerOverrideHttpServletRequest extends HttpServletRequestWrapper {
  private String serverUrl;

  private int serverPort;

  private String serverName;

  private String scheme;

  public ServerOverrideHttpServletRequest(final String serverUrl,
    HttpServletRequest request) {
    super(request);

    try {
      URL url = new URL(serverUrl);
      scheme = url.getProtocol();
      serverName = url.getHost();
      serverPort = url.getPort();
      if (serverPort == -1) {
        serverPort = url.getDefaultPort();
        this.serverUrl = scheme + "://" + serverName;
      } else {
        this.serverUrl = scheme + "://" + serverName + ":" + serverPort;
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL " + serverUrl);
    }

  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public String getServerName() {
    return serverName;
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
