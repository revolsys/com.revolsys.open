package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

public final class HttpServletLogUtil {
  public static void logRequestException(
    final Logger log,
    final HttpServletRequest request,
    final Throwable exception) {
    logRequestException(log, request, exception, null);
  }

  public static void logRequestException(
    final Logger log,
    final HttpServletRequest request,
    final Throwable exception,
    final String[] headerNames) {
    if (!(exception instanceof IOException)) {
      if (request.getAttribute("LogException") != exception) {
        final StringBuffer text = new StringBuffer();
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
          for (int i = 0; i < headerNames.length; i++) {
            final String headerName = headerNames[i];
            final String value = request.getHeader(headerName);
            if (value != null) {
              text.append('\n').append(headerName).append('\t').append(value);
            }
          }
        }
        log.error(text.toString(), exception);
        request.setAttribute("LogException", exception);
      }
    }
  }

  private HttpServletLogUtil() {
  }
}
