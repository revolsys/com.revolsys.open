package com.revolsys.ui.web.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import com.revolsys.converter.string.StringConverterRegistry;

public final class HttpServletUtils {
  private static ThreadLocal<HttpServletRequest> REQUEST_LOCAL = new ThreadLocal<HttpServletRequest>();

  private static ThreadLocal<HttpServletResponse> RESPONSE_LOCAL = new ThreadLocal<HttpServletResponse>();

  public static void clearRequestAndResponse() {
    REQUEST_LOCAL.remove();
    RESPONSE_LOCAL.remove();
  }

  public static String getFullRequestUrl() {
    return getFullRequestUrl(getRequest());
  }

  public static String getFullRequestUrl(final HttpServletRequest request) {
    final String serverUrl = getServerUrl(request);
    final String requestUri = getOriginatingRequestUri();
    return serverUrl + requestUri;
  }

  public static HttpServletRequest getRequest() {
    final HttpServletRequest request = REQUEST_LOCAL.get();
    return request;
  }

  public static HttpServletResponse getResponse() {
    final HttpServletResponse response = RESPONSE_LOCAL.get();
    return response;
  }

  public static <T> T notFound() {
    HttpServletResponse response = getResponse();
    try {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    } catch (IOException e) {
    }
    return null;
  }

  public static <T> T seeOther(String location) {
    HttpServletResponse response = getResponse();
    response.setStatus(HttpServletResponse.SC_SEE_OTHER);
    response.setHeader("Location", location);
    return null;
  }

  public static <T> T sendRedirect(String location) {
    HttpServletResponse response = getResponse();
    try {
      response.sendRedirect(location);
    } catch (IOException e) {
    }
    return null;
  }

  public static String getOriginatingRequestUri() {
    final HttpServletRequest request = getRequest();
    final String originatingRequestUri = new UrlPathHelper().getOriginatingRequestUri(request);
    return originatingRequestUri;
  }

  public static String getPathVariable(final String name) {
    return getPathVariables().get(name);
  }

  public static Map<String, String> getPathVariables() {
    final HttpServletRequest request = getRequest();
    if (request != null) {
      @SuppressWarnings("unchecked")
      Map<String, String> pathVariables = (Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      if (pathVariables == null) {
        pathVariables = new HashMap<String, String>();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
          pathVariables);
      }
      return pathVariables;
    }
    return new HashMap<String, String>();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getRequestAttribute(final String name) {
    final HttpServletRequest request = getRequest();
    if (request == null) {
      return null;
    } else {
      return (T)request.getAttribute(name);
    }
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
    return getServerUrl(getRequest());
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

  public static void setRequestAndResponse(final HttpServletRequest request,
    HttpServletResponse response) {
    REQUEST_LOCAL.set(request);
    RESPONSE_LOCAL.set(response);
  }

  public static void setPathVariable(final String name, final String value) {
    getPathVariables().put(name, value);
  }

  public static void setPathVariable(final String name, final Object value) {
    if (value == null) {
      setPathVariable(name, null);
    } else {
      setPathVariable(name, StringConverterRegistry.toString(value));
    }
  }

  public static int getIntegerParameter(HttpServletRequest request,
    String paramName) {
    String value = request.getParameter(paramName);
    if (StringUtils.hasText(value)) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
      }
    }
    return 0;
  }

  public static boolean getBooleanParameter(HttpServletRequest request,
    String paramName) {
    String value = request.getParameter(paramName);
    if (StringUtils.hasText(value)) {
      return Boolean.parseBoolean(value);
    }
    return false;
  }

  private HttpServletUtils() {

  }

  public static void setAttribute(String name, Object value) {
    HttpServletRequest request = getRequest();
    request.setAttribute(name, value);
  }

  public static void redirect(String url) {
    try {
      HttpServletResponse response = getResponse();
      response.sendRedirect(url);
    } catch (IOException e) {
    }
  }
}
