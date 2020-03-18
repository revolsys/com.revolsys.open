package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.logging.Logs;

import com.revolsys.ui.web.utils.HttpServletUtils;

public final class HttpServletLogUtil {

  public static void logRequestException(final Object logCategory, final HttpServletRequest request,
    final Throwable exception, final String[] headerNames) {
    if (!(exception instanceof IOException) || !exception.getMessage().contains("Broken pipe")) {
      if (request == null) {
        Logs.error(logCategory, exception);
      } else if (request.getAttribute("LogException") != exception) {
        final StringBuilder text = new StringBuilder();
        final String message = exception.getMessage();
        if (message != null) {
          text.append(message);
        }
        final String method = request.getMethod();
        final StringBuffer requestURL = request.getRequestURL();
        final String query = request.getQueryString();

        requestURL.insert(0, method + " ");
        if (query != null) {
          requestURL.append('?').append(query);
        }
        text.append('\n').append("URL\t").append(requestURL);

        final String referer = request.getHeader("Referer");
        if (referer != null) {
          text.append('\n').append("Referer\t").append(referer);
        }

        final String remoteUser = request.getRemoteUser();
        if (remoteUser != null) {
          text.append('\n').append("RemoteUser\t").append(remoteUser);
        }

        if (headerNames != null) {
          for (final String headerName : headerNames) {
            final String value = request.getHeader(headerName);
            if (value != null) {
              text.append('\n').append(headerName).append('\t').append(value);
            }
          }
        }
        Logs.error(logCategory, text.toString(), exception);
        request.setAttribute("LogException", exception);
      }
    }
  }

  public static void logRequestException(final Object logCategory, final ServletRequest request,
    final Throwable exception) {
    HttpServletRequest httpRequest = null;
    if (request instanceof HttpServletRequest) {
      httpRequest = (HttpServletRequest)request;
    }
    logRequestException(logCategory, httpRequest, exception, null);
  }

  public static void logRequestException(final Object logCategory, final Throwable exception) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    logRequestException(logCategory, request, exception, null);
  }

  private HttpServletLogUtil() {
  }
}
