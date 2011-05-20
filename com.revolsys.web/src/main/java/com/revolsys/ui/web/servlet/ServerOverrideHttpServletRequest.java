package com.revolsys.ui.web.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServerOverrideHttpServletRequest extends HttpServletRequestWrapper {
  private String serverUrl;

  private String secureServerUrl;

  private String serverName;

  private String scheme;

  private int serverPort;

  public ServerOverrideHttpServletRequest(final String serverUrl,
    HttpServletRequest request) {
    super(request);

    try {
      URL url = new URL(serverUrl);
      scheme = url.getProtocol();
      serverName = url.getHost();
       serverPort = url.getPort();
      if (serverPort == -1) {
        this.serverPort = url.getDefaultPort();
        this.serverUrl = scheme + "://" + serverName;
        this.secureServerUrl = "https://" + serverName;
      } else {
        this.serverUrl = scheme + "://" + serverName + ":" + serverPort;
        this.secureServerUrl = "https://" + serverName;
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL " + serverUrl);
    }

  }

  @Override
  public String getScheme() {
    if (super.getScheme().equals("https")) {
      return super.getScheme();
    } else {
      return scheme;
    }
  }

  @Override
  public int getServerPort() {
    if (super.getScheme().equals("https")) {
      return super.getServerPort();
    } else {
      return serverPort;
    }
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public StringBuffer getRequestURL() {
    String serverUrl;
    final String scheme = super.getScheme();
    if (scheme.equals("https")) {
      serverUrl = secureServerUrl;
    } else {
      serverUrl = this.serverUrl;
    }
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
