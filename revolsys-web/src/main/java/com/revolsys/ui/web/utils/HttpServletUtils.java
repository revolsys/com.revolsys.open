package com.revolsys.ui.web.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.type.DataTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.FileUtil;
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.util.Property;

public final class HttpServletUtils {
  private static ThreadLocal<HttpServletRequest> REQUEST_LOCAL = new ThreadLocal<>();

  private static ThreadLocal<HttpServletResponse> RESPONSE_LOCAL = new ThreadLocal<>();

  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  public static void clearRequestAndResponse() {
    REQUEST_LOCAL.remove();
    RESPONSE_LOCAL.remove();
  }

  protected static String getAbsoluteUrl(final HttpServletRequest request, final String url) {
    if (url == null) {
      return null;
    } else if (url.startsWith("/")) {
      final String serverUrl = getServerUrl(request);
      final String contextPath = URL_PATH_HELPER.getOriginatingContextPath(request);
      return serverUrl + contextPath + url;
    } else {
      return url;
    }
  }

  public static String getAbsoluteUrl(final String url) {
    final HttpServletRequest request = getRequest();
    return getAbsoluteUrl(request, url);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttribute(final String name) {
    final HttpServletRequest request = getRequest();
    if (request == null) {
      return null;
    } else {
      return (T)request.getAttribute(name);
    }
  }

  public static boolean getBooleanParameter(final HttpServletRequest request,
    final String paramName) {
    final String value = request.getParameter(paramName);
    if (Property.hasValue(value)) {
      return Boolean.parseBoolean(value);
    }
    return false;
  }

  public static String getFullRequestUrl() {
    final HttpServletRequest request = getRequest();
    return getFullRequestUrl(request);
  }

  public static String getFullRequestUrl(final HttpServletRequest request) {
    final String serverUrl = getServerUrl(request);
    final String requestUri = getOriginatingRequestUri();
    return serverUrl + requestUri;
  }

  public static String getFullUrl(final String url) {
    final String aliasUrl = PathAliasController.getPath(url);
    return getAbsoluteUrl(aliasUrl);
  }

  public static int getIntegerParameter(final HttpServletRequest request, final String paramName) {
    final String value = request.getParameter(paramName);
    if (Property.hasValue(value)) {
      try {
        return Integer.parseInt(value);
      } catch (final NumberFormatException e) {
      }
    }
    return 0;
  }

  public static String getOriginatingContextPath() {
    final HttpServletRequest request = getRequest();
    return URL_PATH_HELPER.getOriginatingContextPath(request);
  }

  public static String getOriginatingRequestUri() {
    final HttpServletRequest request = getRequest();
    return getOriginatingRequestUri(request);
  }

  public static String getOriginatingRequestUri(final HttpServletRequest request) {
    final String originatingRequestUri = new UrlPathHelper().getOriginatingRequestUri(request);
    return originatingRequestUri;
  }

  public static String getParameter(final String name) {
    final HttpServletRequest request = getRequest();
    return request.getParameter(name);
  }

  public static Map<String, Object> getParameterMap(final HttpServletRequest request) {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    final Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      final String name = parameterNames.nextElement();
      final String[] values = request.getParameterValues(name);
      if (values.length == 0) {
        parameters.put(name, null);
      } else if (values.length == 1) {
        parameters.put(name, values[0]);
      } else if (values.length == 1) {
        parameters.put(name, Arrays.asList(values));
      }
    }
    return parameters;
  }

  public static String[] getParameterValues(final String name) {
    final HttpServletRequest request = getRequest();
    return request.getParameterValues(name);
  }

  public static String getPathVariable(final String name) {
    return getPathVariables().get(name);
  }

  public static Map<String, String> getPathVariables() {
    final HttpServletRequest request = getRequest();
    if (request != null) {
      @SuppressWarnings("unchecked")
      Map<String, String> pathVariables = (Map<String, String>)request
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      if (pathVariables == null) {
        pathVariables = new HashMap<>();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);
      }
      return pathVariables;
    }
    return new HashMap<>();
  }

  public static HttpServletRequest getRequest() {
    final HttpServletRequest request = REQUEST_LOCAL.get();
    return request;
  }

  public static String getRequestBaseFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = FileUtil.getBaseName(originatingRequestUri);
    return baseName;
  }

  public static String getRequestFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    final String baseName = FileUtil.getBaseName(originatingRequestUri);
    return baseName;
  }

  public static HttpServletResponse getResponse() {
    final HttpServletResponse response = RESPONSE_LOCAL.get();
    return response;
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

  public static boolean isApiCall() {
    final HttpServletRequest request = getRequest();
    return isApiCall(request);
  }

  public static boolean isApiCall(final HttpServletRequest request) {
    final String requestedWith = request.getHeader("x-requested-with");
    if (Property.hasValue(requestedWith)) {
      return true;
    } else {
      final String referrer = request.getHeader("referrer");
      if (Property.hasValue(referrer)) {
        return false;
      } else {
        final String accept = request.getHeader("accept");
        if (accept == null) {
          return true;
        } else {
          return !accept.contains("*/*");
        }
      }
    }
  }

  public static <T> T notFound() {
    final HttpServletResponse response = getResponse();
    try {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    } catch (final IOException e) {
    }
    return null;
  }

  public static void redirect(final String url) {
    try {
      final HttpServletResponse response = getResponse();
      response.sendRedirect(url);
    } catch (final IOException e) {
    }
  }

  public static <T> T seeOther(final String location) {
    final HttpServletResponse response = getResponse();
    response.setStatus(HttpServletResponse.SC_SEE_OTHER);
    response.setHeader("Location", location);
    return null;
  }

  public static <T> T sendRedirect(final String location) {
    final HttpServletResponse response = getResponse();
    try {
      response.sendRedirect(location);
    } catch (final IOException e) {
    }
    return null;
  }

  public static void setAttribute(final String name, final Object value) {
    final HttpServletRequest request = getRequest();
    request.setAttribute(name, value);
  }

  public static Charset setContentTypeWithCharset(final HttpHeaders headers, MediaType mediaType) {
    Charset charset = mediaType.getCharset();
    if (charset == null) {
      charset = StandardCharsets.UTF_8;
      final Map<String, String> params = Collections.singletonMap("charset", "utf-8");
      mediaType = new MediaType(mediaType, params);
    }
    headers.setContentType(mediaType);
    return charset;
  }

  public static Charset setContentTypeWithCharset(final HttpOutputMessage outputMessage,
    final MediaType mediaType) {
    final HttpHeaders headers = outputMessage.getHeaders();
    return setContentTypeWithCharset(headers, mediaType);
  }

  public static void setPathVariable(final String name, final Object value) {
    if (value == null) {
      setPathVariable(name, null);
    } else {
      setPathVariable(name, DataTypes.toString(value));
    }
  }

  public static void setPathVariable(final String name, final String value) {
    getPathVariables().put(name, value);
  }

  public static void setRequestAndResponse(final HttpServletRequest request,
    final HttpServletResponse response) {
    REQUEST_LOCAL.set(request);
    RESPONSE_LOCAL.set(response);
  }

  private HttpServletUtils() {

  }
}
