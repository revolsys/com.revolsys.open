package com.revolsys.ui.web.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ServerOverrideHttpServletRequest extends HttpServletRequestWrapper {
  private String scheme;

  private String secureServerUrl;

  private String serverName;

  private int serverPort;

  private String serverUrl;

  public ServerOverrideHttpServletRequest(final String serverUrl,
    final HttpServletRequest request) {
    super(request);

    try {
      final URL url = new URL(serverUrl);
      this.scheme = url.getProtocol();
      this.serverName = url.getHost();
      this.serverPort = url.getPort();
      if (this.serverPort == -1) {
        this.serverPort = url.getDefaultPort();
        this.serverUrl = this.scheme + "://" + this.serverName;
        this.secureServerUrl = "https://" + this.serverName;
      } else {
        this.serverUrl = this.scheme + "://" + this.serverName + ":" + this.serverPort;
        this.secureServerUrl = "https://" + this.serverName;
      }
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL " + serverUrl);
    }

  }

  @Override
  public StringBuffer getRequestURL() {
    String serverUrl;
    final String scheme = super.getScheme();
    if (scheme.equals("https")) {
      serverUrl = this.secureServerUrl;
    } else {
      serverUrl = this.serverUrl;
    }
    final StringBuffer url = new StringBuffer(serverUrl);
    final String contextPath = getContextPath();
    if (contextPath != null) {
      url.append(contextPath);
    }
    final String servletPath = getServletPath();
    if (servletPath != null) {
      url.append(servletPath);
    }
    final String pathInfo = getPathInfo();
    if (pathInfo != null) {
      url.append(pathInfo);
    }
    return url;
  }

  @Override
  public String getScheme() {
    if (super.getScheme().equals("https")) {
      return super.getScheme();
    } else {
      return this.scheme;
    }
  }

  @Override
  public String getServerName() {
    return this.serverName;
  }

  @Override
  public int getServerPort() {
    if (super.getScheme().equals("https")) {
      return super.getServerPort();
    } else {
      return this.serverPort;
    }
  }
}
