package com.revolsys.ui.web.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

public final class HttpRequestUtils {
  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  private HttpRequestUtils() {

  }

  public static HttpServletRequest getHttpServletRequest() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = requestAttributes.getRequest();
    return request;
  }

  public static String getRequestFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    String baseName = WebUtils.extractFullFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getRequestBaseFileName() {
    final String originatingRequestUri = getOriginatingRequestUri();
    String baseName = WebUtils.extractFilenameFromUrlPath(originatingRequestUri);
    return baseName;
  }

  public static String getOriginatingRequestUri() {
    final HttpServletRequest request = getHttpServletRequest();
    final String originatingRequestUri = URL_PATH_HELPER.getOriginatingRequestUri(request);
    return originatingRequestUri;
  }
}
