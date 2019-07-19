package com.revolsys.ui.web.rest.interceptor;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.WebUtils;

import com.revolsys.ui.web.annotation.RequestMapping;

/**
 * Helper class for annotation-based request mapping.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
abstract class ServletAnnotationMappingUtils {

  /**
   * Check whether the given request matches the specified header conditions.
   *
   * @param headers the header conditions, following
   *          {@link RequestMapping#headers()}
   * @param request the current HTTP request to check
   */
  public static boolean checkHeaders(final String[] headers, final HttpServletRequest request) {
    if (!ObjectUtils.isEmpty(headers)) {
      for (final String header : headers) {
        final int separator = header.indexOf('=');
        if (separator == -1) {
          if (header.startsWith("!")) {
            if (request.getHeader(header.substring(1)) != null) {
              return false;
            }
          } else if (request.getHeader(header) == null) {
            return false;
          }
        } else {
          final String key = header.substring(0, separator);
          final String value = header.substring(separator + 1);
          if (isMediaTypeHeader(key)) {
            final List<MediaType> requestMediaTypes = MediaType
              .parseMediaTypes(request.getHeader(key));
            final List<MediaType> valueMediaTypes = MediaType.parseMediaTypes(value);
            boolean found = false;
            for (final Iterator<MediaType> valIter = valueMediaTypes.iterator(); valIter.hasNext()
              && !found;) {
              final MediaType valueMediaType = valIter.next();
              for (final Iterator<MediaType> reqIter = requestMediaTypes.iterator(); reqIter
                .hasNext() && !found;) {
                final MediaType requestMediaType = reqIter.next();
                if (valueMediaType.includes(requestMediaType)) {
                  found = true;
                }
              }

            }
            if (!found) {
              return false;
            }
          } else if (!value.equals(request.getHeader(key))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Check whether the given request matches the specified parameter conditions.
   *
   * @param params the parameter conditions, following
   *          {@link RequestMapping#params()}
   * @param request the current HTTP request to check
   */
  public static boolean checkParameters(final String[] params, final HttpServletRequest request) {
    if (!ObjectUtils.isEmpty(params)) {
      for (final String param : params) {
        final int separator = param.indexOf('=');
        if (separator == -1) {
          if (param.startsWith("!")) {
            if (WebUtils.hasSubmitParameter(request, param.substring(1))) {
              return false;
            }
          } else if (!WebUtils.hasSubmitParameter(request, param)) {
            return false;
          }
        } else {
          final String key = param.substring(0, separator);
          final String value = param.substring(separator + 1);
          if (!value.equals(request.getParameter(key))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Check whether the given request matches the specified request methods.
   *
   * @param methods the HTTP request methods to check against
   * @param request the current HTTP request to check
   */
  public static boolean checkRequestMethod(final RequestMethod[] methods,
    final HttpServletRequest request) {
    if (ObjectUtils.isEmpty(methods)) {
      return true;
    }
    for (final RequestMethod method : methods) {
      if (method.name().equals(request.getMethod())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isMediaTypeHeader(final String headerName) {
    return "Accept".equalsIgnoreCase(headerName) || "Content-Type".equalsIgnoreCase(headerName);
  }

}
