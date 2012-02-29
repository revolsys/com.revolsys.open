package com.revolsys.ui.web.utils;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

public final class HttpRequestUtils {
  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  private static ThreadLocal<HttpServletRequest> REQUEST_LOCAL = new ThreadLocal<HttpServletRequest>();

  public static String getFullRequestUrl() {
    return getFullRequestUrl(getHttpServletRequest());
  }

  public static String getFullRequestUrl(final HttpServletRequest request) {
    final String serverUrl = getServerUrl(request);
    final String requestUri = getOriginatingRequestUri();
    return serverUrl + requestUri;
  }

  public static HttpServletRequest getHttpServletRequest() {
    HttpServletRequest request = REQUEST_LOCAL.get();
    return request;
  }

  public static Map<String, String> getPathVariables() {
    final HttpServletRequest request = getHttpServletRequest();
    if (request != null) {
      @SuppressWarnings("unchecked")
      final Map<String, String> pathVariables = (Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      if (pathVariables != null) {
        return pathVariables;
      }
    }
    return Collections.emptyMap();
  }

  public static void setHttpServletRequest(HttpServletRequest request) {
    REQUEST_LOCAL.set(request);
  }

  public static void clearHttpServletRequest() {
    REQUEST_LOCAL.remove();
  }

  public static String getOriginatingRequestUri() {
    final HttpServletRequest request = getHttpServletRequest();
    final String originatingRequestUri = URL_PATH_HELPER.getOriginatingRequestUri(request);
    return originatingRequestUri;
  }

  public static String getRequestBaseFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = WebUtils.extractFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getRequestFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = WebUtils.extractFullFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getServerUrl() {
    return getServerUrl(getHttpServletRequest());
  }

  public static String getServerUrl(final HttpServletRequest request) {
    final String scheme = request.getScheme();
    final String serverName = request.getServerName();
    final int serverPort = request.getServerPort();
    final StringBuilder url = new StringBuilder();
    url.append(scheme);
    url.append("://");
    url.append(serverName);

    if ("http".equals(scheme)) {
      if (serverPort != 80 && serverPort != -1) {
        url.append(":").append(serverPort);
      }
    } else if ("https".equals(scheme)) {
      if (serverPort != 443 && serverPort != -1) {
        url.append(":").append(serverPort);
      }
    }
    return url.toString();

  }

  private HttpRequestUtils() {

  }
}
